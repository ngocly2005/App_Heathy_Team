from datetime import date, datetime
from typing import Literal
from pydantic import BaseModel, ConfigDict, EmailStr, Field, field_validator


class UserResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    email: EmailStr
    full_name: str
    status: str
    google_sub: str
    created_at: datetime


class GoogleLoginRequest(BaseModel):
    id_token: str = Field(min_length=20, max_length=10000)


class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


class RefreshRequest(BaseModel):
    refresh_token: str = Field(min_length=20)


class MessageResponse(BaseModel):
    message: str


Sex = Literal["male", "female"]


ActivityLevel = Literal["sedentary", "light", "moderate", "very_active"]


HealthGoal = Literal["lose", "maintain", "gain"]


class ProfileUpsert(BaseModel):
    birth_date: date
    sex: Sex
    height_cm: float = Field(gt=80, le=250)
    weight_kg: float = Field(gt=20, le=400)
    activity_level: ActivityLevel
    goal: HealthGoal = "maintain"

    @field_validator("birth_date")
    @classmethod
    def birth_date_must_be_in_the_past(cls, birth_date: date) -> date:
        if birth_date >= date.today():
            raise ValueError("Birth date must be in the past")
        return birth_date


class ProfileResponse(BaseModel):
    user_id: int
    birth_date: date
    age: int
    sex: str
    height_cm: float
    weight_kg: float
    activity_level: str
    goal: str
    bmi: float
    bmr_kcal: float
    tdee_kcal: float
    updated_at: datetime


MealType = Literal["breakfast", "lunch", "dinner", "snack"]


class FoodLogResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    food_id: int | None
    log_date: date
    meal_type: str
    grams: float
    food_name: str
    calories_kcal: float
    protein_g: float
    carb_g: float
    fat_g: float
    created_at: datetime


class DailyDashboardResponse(BaseModel):
    date: date
    calories_consumed: float
    calories_burned: float
    net_calories: float
    calorie_target: float | None
    calories_remaining: float | None
    water_ml: int
    food_logs: list[FoodLogResponse]
