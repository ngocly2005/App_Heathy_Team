from datetime import date
from fastapi import APIRouter, Query
from sqlalchemy import select
from app.calculations import calculate_age, calculate_bmr, calculate_tdee
from app.dependencies import CurrentUser, DbSession
from app.models import FoodLog, HealthProfile
from app.schemas import DailyDashboardResponse, FoodLogResponse


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
    return DailyDashboardResponse(
        date=selected_date,
        calories_consumed=consumed,
        calories_burned=burned,
        net_calories=net_calories,
        calorie_target=calorie_target,
        calories_remaining=calories_remaining,
        water_ml=water_ml,
        food_logs=[FoodLogResponse.model_validate(item) for item in food_logs],
    )
