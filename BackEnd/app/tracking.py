from datetime import date
from fastapi import APIRouter, HTTPException, Query, Response, status
from sqlalchemy import select
from app.calculations import (
    calculate_age,
    calculate_bmr,
    calculate_macro_percentages,
    calculate_tdee,
    scale_nutrition,
)
from app.dependencies import CurrentUser, DbSession
from app.models import Food, FoodLog, HealthProfile
from app.schemas import (
    DailyDashboardResponse,
    FoodLogCreate,
    FoodLogResponse,
    FoodResponse,
    MacroSummary,
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
    activity_logs = []
    water_ml = 0

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
    )
