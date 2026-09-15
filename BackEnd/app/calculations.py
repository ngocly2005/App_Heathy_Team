from datetime import date


ACTIVITY_FACTORS = {
    "sedentary": 1.2,
    "light": 1.375,
    "moderate": 1.55,
    "very_active": 1.725,
}


def calculate_age(birth_date: date, today: date | None = None) -> int:
    reference_date = today or date.today()
    birthday_passed = (reference_date.month, reference_date.day) >= (
        birth_date.month,
        birth_date.day,
    )
    return reference_date.year - birth_date.year - (not birthday_passed)


def calculate_bmi(weight_kg: float, height_cm: float) -> float:
    height_m = height_cm / 100
    return round(weight_kg / (height_m**2), 2)


def calculate_bmr(
    weight_kg: float,
    height_cm: float,
    age: int,
    sex: str,
) -> float:
    sex_adjustment = 5 if sex == "male" else -161
    result = 10 * weight_kg + 6.25 * height_cm - 5 * age + sex_adjustment
    return round(result, 2)


def calculate_tdee(bmr_kcal: float, activity_level: str) -> float:
    return round(bmr_kcal * ACTIVITY_FACTORS[activity_level], 2)
