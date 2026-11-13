from app.core.dates import today as business_today
from datetime import UTC, date, datetime, timedelta
from typing import Literal
from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, ConfigDict, Field, ValidationError
from sqlalchemy import func, or_, select
from sqlalchemy.exc import IntegrityError
from app.dependencies import AdminUser, DbSession
from app.models import AIInteraction, RefreshToken, User
from app.schemas import AIInteractionResponse, UserResponse


router = APIRouter(prefix="/admin", tags=["admin"])


Resource = Literal["users"]


MODELS = {"users": User}


class FoodInput(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True, allow_inf_nan=False)
    name: str = Field(min_length=1, max_length=160)
    calories_per_100g: float = Field(ge=0, le=20000)
    protein_per_100g: float = Field(ge=0, le=2000)
    carb_per_100g: float = Field(ge=0, le=3000)
    fat_per_100g: float = Field(ge=0, le=2000)
    is_active: bool = True


class ActivityInput(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True, allow_inf_nan=False)
    name: str = Field(min_length=1, max_length=120)
    met: float = Field(ge=0.01, le=30)
    is_active: bool = True


class StatusInput(BaseModel):
    model_config = ConfigDict(extra="forbid")
    status: Literal["active", "disabled"]


def serialize(resource, row):
    if resource == "users":
        return UserResponse.model_validate(row).model_dump(mode="json")
    if resource == "ai-logs":
        return AIInteractionResponse.model_validate(row).model_dump(mode="json")
    names = FoodInput.model_fields if resource == "foods" else ActivityInput.model_fields
    return {"id": row.id, **{key: getattr(row, key) for key in names}}


def date_range(date_from, date_to):
    end = date_to or business_today()
    start = date_from or end - timedelta(days=29)
    if start > end or (end - start).days > 365:
        raise HTTPException(422, "Choose an ordered date range of at most 366 days")
    return start, end


@router.get("/{resource}")
def list_resource(resource: Resource, current_user: AdminUser, db: DbSession,
                  page: int = Query(1, ge=1), per_page: int = Query(25, ge=1, le=100),
                  q: str = Query("", max_length=100), sort_field: str = "id",
                  sort_order: Literal["ASC", "DESC"] = "ASC", user_id: int | None = None,
                  kind: Literal["chat", "photo"] | None = None, date_from: date | None = None,
                  date_to: date | None = None, is_active: bool | None = None,
                  status: Literal["active", "disabled"] | None = None):
    model = MODELS[resource]
    statement = select(model)
    if resource == "users":
        if q:
            statement = statement.where(or_(User.full_name.icontains(q, autoescape=True), User.email.icontains(q, autoescape=True)))
        if status:
            statement = statement.where(User.status == status)
    elif resource == "ai-logs":
        if user_id is not None:
            statement = statement.where(AIInteraction.user_id == user_id)
        if kind:
            statement = statement.where(AIInteraction.kind == kind)
        if date_from:
            statement = statement.where(AIInteraction.context_date >= date_from)
        if date_to:
            statement = statement.where(AIInteraction.context_date <= date_to)
        if date_from and date_to and date_from > date_to:
            raise HTTPException(422, "Invalid date range")
    else:
        if q:
            statement = statement.where(model.name.icontains(q, autoescape=True))
        if is_active is not None:
            statement = statement.where(model.is_active == is_active)
    if sort_field not in model.__table__.columns:
        raise HTTPException(422, "Unknown sort field")
    total = db.scalar(select(func.count()).select_from(statement.subquery())) or 0
    order = getattr(model, sort_field)
    order = order.desc() if sort_order == "DESC" else order.asc()
    rows = list(db.scalars(statement.order_by(order, model.id).offset((page-1)*per_page).limit(per_page)))
    return {"items": [serialize(resource, row) for row in rows], "total": total}


@router.get("/{resource}/{item_id}")
def read_resource(resource: Resource, item_id: int, current_user: AdminUser, db: DbSession):
    row = db.get(MODELS[resource], item_id)
    if row is None:
        raise HTTPException(404, "Record not found")
    return serialize(resource, row)


def validate(resource, payload):
    schema = FoodInput if resource == "foods" else ActivityInput if resource == "activities" else StatusInput
    try:
        return schema.model_validate(payload).model_dump()
    except ValidationError as exc:
        raise HTTPException(422, exc.errors(include_context=False)) from exc


def commit(db):
    try:
        db.commit()
    except IntegrityError as exc:
        db.rollback()
        raise HTTPException(409, "A record with this name already exists") from exc


@router.put("/{resource}/{item_id}")
def update_resource(resource: Literal["users"], item_id: int,
                    payload: dict, current_user: AdminUser, db: DbSession):
    row = db.scalar(select(MODELS[resource]).where(MODELS[resource].id == item_id).with_for_update())
    if row is None:
        raise HTTPException(404, "Record not found")
    values = validate(resource, payload)
    if resource == "users" and values["status"] == "disabled":
        if item_id == current_user.id or row.role == "admin":
            raise HTTPException(409, "Admin accounts cannot be disabled here")
        for token in db.scalars(select(RefreshToken).where(RefreshToken.user_id == item_id, RefreshToken.revoked_at.is_(None))):
            token.revoked_at = datetime.now(UTC)
    for key, value in values.items():
        setattr(row, key, value)
    commit(db)
    db.refresh(row)
    return serialize(resource, row)
