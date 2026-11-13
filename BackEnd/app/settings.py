from fastapi import APIRouter
from sqlalchemy import select
from app.calculations import calculate_age, calculate_bmr, calculate_tdee
from app.dependencies import CurrentUser, DbSession
from app.models import HealthProfile, NutritionSettings, User
from app.schemas import NutritionSettingsUpsert


router = APIRouter(tags=["settings"])


def calorie_target(db, user_id):
    custom = db.get(NutritionSettings, user_id)
    if custom and custom.calorie_target is not None:
        return float(custom.calorie_target)
    profile = db.get(HealthProfile, user_id)
    if profile is None:
        return None
    return calculate_tdee(calculate_bmr(float(profile.weight_kg), float(profile.height_cm),
                                      calculate_age(profile.birth_date), profile.sex), profile.activity_level)


def nutrition_response(db, user_id):
    row = db.get(NutritionSettings, user_id)
    values = ({key: getattr(row, key) for key in NutritionSettingsUpsert.model_fields}
              if row else NutritionSettingsUpsert().model_dump())
    effective = calorie_target(db, user_id)
    return {**values, "effective_calorie_target": effective,
            "protein_g": round((effective or 0) * values["protein_pct"] / 100 / 4, 2),
            "carb_g": round((effective or 0) * values["carb_pct"] / 100 / 4, 2),
            "fat_g": round((effective or 0) * values["fat_pct"] / 100 / 9, 2)}


@router.get("/nutrition-settings")
def read_nutrition(current_user: CurrentUser, db: DbSession):
    return nutrition_response(db, current_user.id)


@router.put("/nutrition-settings")
def write_nutrition(payload: NutritionSettingsUpsert, current_user: CurrentUser, db: DbSession):
    db.scalar(select(User).where(User.id == current_user.id).with_for_update())
    row = db.get(NutritionSettings, current_user.id)
    if row is None:
        row = NutritionSettings(user_id=current_user.id)
        db.add(row)
    for key, value in payload.model_dump().items():
        setattr(row, key, value)
    db.commit()
    return nutrition_response(db, current_user.id)
