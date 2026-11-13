from fastapi import APIRouter, HTTPException
from sqlalchemy import select
from app.dependencies import CurrentUser, DbSession
from app.models import FoodLog
from app.schemas import FoodLogResponse, FoodLogUpdate


router = APIRouter(tags=["editable logs"])


def owned(db, model, item_id, user_id):
    row = db.scalar(select(model).where(model.id == item_id, model.user_id == user_id).with_for_update())
    if row is None:
        raise HTTPException(404, "Log not found")
    return row


@router.put("/food-logs/{log_id}", response_model=FoodLogResponse)
def update_food(log_id: int, payload: FoodLogUpdate, current_user: CurrentUser, db: DbSession):
    row = owned(db, FoodLog, log_id, current_user.id)
    ratio = payload.grams / float(row.grams)
    for field in ("calories_kcal", "protein_g", "carb_g", "fat_g"):
        setattr(row, field, round(float(getattr(row, field)) * ratio, 2))
    row.grams, row.meal_type, row.meal_time = payload.grams, payload.meal_type, payload.meal_time
    db.commit()
    db.refresh(row)
    return row
