from datetime import date
from decimal import ROUND_HALF_UP, Decimal
from fastapi import APIRouter, HTTPException, Query, Response, status
from sqlalchemy import func, select
from app.calculations import (
    calculate_activity_calories,
    calculate_age,
    calculate_bmr,
    calculate_macro_percentages,
    calculate_tdee,
    scale_nutrition,
)
from app.dependencies import CurrentUser, DbSession
from app.models import (
    ActivityLog,
    ActivityType,
    Food,
    FoodLog,
    HealthProfile,
    User,
    WaterLog,
    WaterSettings,
)
from app.schemas import (
    ActivityLogCreate,
    ActivityLogResponse,
    ActivityTypeResponse,
    DailyDashboardResponse,
    FoodLogCreate,
    FoodLogResponse,
    FoodResponse,
    MacroSummary,
    WaterAdjustmentResponse,
    WaterLogAdjust,
    WaterLogCreate,
    WaterLogResponse,
    WaterSettingsResponse,
    WaterSettingsUpdate,
)


router = APIRouter(tags=["tracking"])


def get_calorie_target(db: DbSession, user_id: int) -> float | None:
    profile = db.get(HealthProfile, user_id)
    if profile is None:
        return None
    age = calculate_age(profile.birth_date)
    bmr = calculate_bmr(
        float(profile.weight_kg),
        float(profile.height_cm),
        age,
        profile.sex,
    )
    return calculate_tdee(bmr, profile.activity_level)


@router.get("/foods", response_model=list[FoodResponse])
def list_foods(
    current_user: CurrentUser,
    db: DbSession,
    query: str | None = Query(default=None, alias="q", max_length=100),
) -> list[Food]:
    statement = select(Food).where(Food.is_active.is_(True))
    if query:
        statement = statement.where(Food.name.icontains(query, autoescape=True))
    return list(db.scalars(statement.order_by(Food.name).limit(50)))


@router.post(
    "/food-logs",
    response_model=FoodLogResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_food_log(
    payload: FoodLogCreate,
    current_user: CurrentUser,
    db: DbSession,
) -> FoodLog:
    food = db.get(Food, payload.food_id)
    if food is None or not food.is_active:
        raise HTTPException(status_code=404, detail="Food not found")

    grams = payload.grams
    food_log = FoodLog(
        user_id=current_user.id,
        food_id=food.id,
        log_date=payload.log_date,
        meal_type=payload.meal_type,
        grams=grams,
        food_name=food.name,
        calories_kcal=scale_nutrition(float(food.calories_per_100g), grams),
        protein_g=scale_nutrition(float(food.protein_per_100g), grams),
        carb_g=scale_nutrition(float(food.carb_per_100g), grams),
        fat_g=scale_nutrition(float(food.fat_per_100g), grams),
    )
    db.add(food_log)
    db.commit()
    db.refresh(food_log)
    return food_log


@router.get("/food-logs", response_model=list[FoodLogResponse])
def list_food_logs(
    current_user: CurrentUser,
    db: DbSession,
    log_date: date | None = Query(default=None, alias="date"),
) -> list[FoodLog]:
    selected_date = log_date or date.today()
    return list(
        db.scalars(
            select(FoodLog)
            .where(
                FoodLog.user_id == current_user.id,
                FoodLog.log_date == selected_date,
            )
            .order_by(FoodLog.created_at, FoodLog.id)
        )
    )


@router.delete("/food-logs/{log_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_food_log(
    log_id: int,
    current_user: CurrentUser,
    db: DbSession,
) -> Response:
    food_log = db.get(FoodLog, log_id)
    if food_log is None or food_log.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Food log not found")
    db.delete(food_log)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


def _lock_water_user(db: DbSession, user_id: int) -> None:
    # A stable per-user lock also serializes an empty day and first-time settings.
    # Every water write takes this lock before reading or changing water rows.
    locked_id = db.scalar(select(User.id).where(User.id == user_id).with_for_update())
    if locked_id is None:
        raise HTTPException(status_code=401, detail="Could not validate credentials")


def _water_settings_response(db: DbSession, user_id: int) -> WaterSettingsResponse:
    settings = db.get(WaterSettings, user_id)
    override = settings.target_ml if settings is not None else None
    target = 2000
    if override is not None:
        target = override
    else:
        profile = db.get(HealthProfile, user_id)
        if profile is not None:
            bonus = {"moderate": 300, "very_active": 500}.get(profile.activity_level, 0)
            calculated = Decimal(str(profile.weight_kg)) * 35 + bonus
            rounded_ml = int(calculated.quantize(Decimal("1"), rounding=ROUND_HALF_UP))
            target = max(1500, min(4500, rounded_ml))
    return WaterSettingsResponse(
        target_ml=target,
        override_target_ml=override,
        serving_ml=settings.serving_ml if settings is not None else 200,
        source="custom" if override is not None else "automatic",
    )


@router.get("/water-settings", response_model=WaterSettingsResponse)
def get_water_settings(current_user: CurrentUser, db: DbSession) -> WaterSettingsResponse:
    # Reading defaults must not create a row or require a write transaction.
    return _water_settings_response(db, current_user.id)


@router.put("/water-settings", response_model=WaterSettingsResponse)
def update_water_settings(
    payload: WaterSettingsUpdate, current_user: CurrentUser, db: DbSession
) -> WaterSettingsResponse:
    _lock_water_user(db, current_user.id)
    settings = db.get(WaterSettings, current_user.id)
    if settings is None:
        settings = WaterSettings(user_id=current_user.id)
        db.add(settings)
    settings.target_ml = payload.target_ml
    settings.serving_ml = payload.serving_ml
    db.flush()
    response = _water_settings_response(db, current_user.id)
    db.commit()
    return response


@router.post("/water-logs/adjust", response_model=WaterAdjustmentResponse)
def adjust_water_logs(
    payload: WaterLogAdjust, current_user: CurrentUser, db: DbSession
) -> WaterAdjustmentResponse:
    _lock_water_user(db, current_user.id)
    logs = list(
        db.scalars(
            select(WaterLog)
            .where(WaterLog.user_id == current_user.id, WaterLog.log_date == payload.log_date)
            .order_by(WaterLog.created_at.desc(), WaterLog.id.desc())
            .with_for_update()
        )
    )
    total = sum(log.amount_ml for log in logs)
    applied = payload.amount_ml if payload.amount_ml > 0 else -min(-payload.amount_ml, total)
    if applied > 0:
        db.add(WaterLog(user_id=current_user.id, log_date=payload.log_date, amount_ml=applied))
    elif applied < 0:
        remaining = -applied
        for log in logs:
            if remaining == 0:
                break
            consumed = min(log.amount_ml, remaining)
            remaining -= consumed
            if consumed == log.amount_ml:
                db.delete(log)
            else:
                log.amount_ml -= consumed
    db.commit()
    return WaterAdjustmentResponse(
        date=payload.log_date, water_ml=total + applied, applied_amount_ml=applied
    )


@router.post(
    "/water-logs",
    response_model=WaterLogResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_water_log(
    payload: WaterLogCreate,
    current_user: CurrentUser,
    db: DbSession,
) -> WaterLogResponse:
    _lock_water_user(db, current_user.id)
    water_log = WaterLog(user_id=current_user.id, **payload.model_dump())
    db.add(water_log)
    # Snapshot before releasing the user lock: a subsequent decrement may delete
    # this row immediately, so refreshing after commit can race with that request.
    db.flush()
    response = WaterLogResponse.model_validate(water_log)
    db.commit()
    return response


@router.get("/water-logs", response_model=list[WaterLogResponse])
def list_water_logs(
    current_user: CurrentUser,
    db: DbSession,
    log_date: date | None = Query(default=None, alias="date"),
) -> list[WaterLog]:
    selected_date = log_date or date.today()
    return list(
        db.scalars(
            select(WaterLog)
            .where(
                WaterLog.user_id == current_user.id,
                WaterLog.log_date == selected_date,
            )
            .order_by(WaterLog.created_at, WaterLog.id)
        )
    )


@router.delete("/water-logs/{log_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_water_log(
    log_id: int,
    current_user: CurrentUser,
    db: DbSession,
) -> Response:
    _lock_water_user(db, current_user.id)
    water_log = db.get(WaterLog, log_id)
    if water_log is None or water_log.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Water log not found")
    db.delete(water_log)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/activity-types", response_model=list[ActivityTypeResponse])
def list_activity_types(
    current_user: CurrentUser,
    db: DbSession,
) -> list[ActivityType]:
    statement = (
        select(ActivityType)
        .where(ActivityType.is_active.is_(True))
        .order_by(ActivityType.name)
    )
    return list(db.scalars(statement))


@router.post(
    "/activity-logs",
    response_model=ActivityLogResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_activity_log(
    payload: ActivityLogCreate,
    current_user: CurrentUser,
    db: DbSession,
) -> ActivityLog:
    activity_type = db.get(ActivityType, payload.activity_type_id)
    if activity_type is None or not activity_type.is_active:
        raise HTTPException(status_code=404, detail="Activity type not found")

    profile = db.get(HealthProfile, current_user.id)
    if profile is None:
        raise HTTPException(status_code=409, detail="Create a health profile first")

    met = float(activity_type.met)
    activity_log = ActivityLog(
        user_id=current_user.id,
        activity_type_id=activity_type.id,
        log_date=payload.log_date,
        duration_minutes=payload.duration_minutes,
        activity_name=activity_type.name,
        met_snapshot=met,
        calories_burned=calculate_activity_calories(
            met,
            float(profile.weight_kg),
            payload.duration_minutes,
        ),
    )
    db.add(activity_log)
    db.commit()
    db.refresh(activity_log)
    return activity_log


@router.get("/activity-logs", response_model=list[ActivityLogResponse])
def list_activity_logs(
    current_user: CurrentUser,
    db: DbSession,
    log_date: date | None = Query(default=None, alias="date"),
) -> list[ActivityLog]:
    selected_date = log_date or date.today()
    return list(
        db.scalars(
            select(ActivityLog)
            .where(
                ActivityLog.user_id == current_user.id,
                ActivityLog.log_date == selected_date,
            )
            .order_by(ActivityLog.created_at, ActivityLog.id)
        )
    )


@router.delete("/activity-logs/{log_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_activity_log(
    log_id: int,
    current_user: CurrentUser,
    db: DbSession,
) -> Response:
    activity_log = db.get(ActivityLog, log_id)
    if activity_log is None or activity_log.user_id != current_user.id:
        raise HTTPException(status_code=404, detail="Activity log not found")
    db.delete(activity_log)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("/dashboard/daily", response_model=DailyDashboardResponse)
def daily_dashboard(
    current_user: CurrentUser,
    db: DbSession,
    dashboard_date: date | None = Query(default=None, alias="date"),
) -> DailyDashboardResponse:
    selected_date = dashboard_date or date.today()
    food_logs = list(
        db.scalars(
            select(FoodLog).where(
                FoodLog.user_id == current_user.id,
                FoodLog.log_date == selected_date,
            )
        )
    )
    activity_logs = list(
        db.scalars(
            select(ActivityLog).where(
                ActivityLog.user_id == current_user.id,
                ActivityLog.log_date == selected_date,
            )
        )
    )
    water_ml = int(
        db.scalar(
            select(func.coalesce(func.sum(WaterLog.amount_ml), 0)).where(
                WaterLog.user_id == current_user.id,
                WaterLog.log_date == selected_date,
            )
        )
        or 0
    )

    consumed = round(sum(float(item.calories_kcal) for item in food_logs), 2)
    burned = round(sum(float(item.calories_burned) for item in activity_logs), 2)
    net_calories = round(consumed - burned, 2)
    calorie_target = get_calorie_target(db, current_user.id)
    calories_remaining = (
        round(calorie_target - net_calories, 2)
        if calorie_target is not None
        else None
    )
    protein_g = round(sum(float(item.protein_g) for item in food_logs), 2)
    carb_g = round(sum(float(item.carb_g) for item in food_logs), 2)
    fat_g = round(sum(float(item.fat_g) for item in food_logs), 2)
    protein_pct, carb_pct, fat_pct = calculate_macro_percentages(
        protein_g, carb_g, fat_g
    )
    return DailyDashboardResponse(
        date=selected_date,
        calories_consumed=consumed,
        calories_burned=burned,
        net_calories=net_calories,
        calorie_target=calorie_target,
        calories_remaining=calories_remaining,
        water_ml=water_ml,
        macros=MacroSummary(
            protein_g=protein_g,
            carb_g=carb_g,
            fat_g=fat_g,
            protein_pct=protein_pct,
            carb_pct=carb_pct,
            fat_pct=fat_pct,
        ),
        food_logs=[FoodLogResponse.model_validate(item) for item in food_logs],
        activity_logs=[
            ActivityLogResponse.model_validate(item) for item in activity_logs
        ],
    )
