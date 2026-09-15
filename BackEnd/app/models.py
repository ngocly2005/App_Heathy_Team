from datetime import UTC, date, datetime
from sqlalchemy import (
    CheckConstraint,
    Date,
    DateTime,
    ForeignKey,
    Integer,
    Numeric,
    String,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column
from app.core.database import Base


def utc_now() -> datetime:
    return datetime.now(UTC)


class User(Base):
    __tablename__ = "users"
    __table_args__ = (
        CheckConstraint("status IN ('active', 'disabled')", name="ck_users_status"),
        UniqueConstraint("google_sub", name="uq_users_google_sub"),
    )

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    email: Mapped[str] = mapped_column(String(320), unique=True)
    full_name: Mapped[str] = mapped_column(String(120))
    google_sub: Mapped[str] = mapped_column(String(255))
    status: Mapped[str] = mapped_column(String(20), default="active")
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utc_now, onupdate=utc_now
    )


class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    user_id: Mapped[int] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"),
        index=True,
    )
    jti: Mapped[str] = mapped_column(String(36), unique=True)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True))
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)


class HealthProfile(Base):
    __tablename__ = "health_profiles"
    __table_args__ = (
        CheckConstraint("sex IN ('male', 'female')", name="ck_health_profile_sex"),
        CheckConstraint("height_cm > 0", name="ck_health_profile_height"),
        CheckConstraint("weight_kg > 0", name="ck_health_profile_weight"),
        CheckConstraint(
            "activity_level IN ('sedentary', 'light', 'moderate', 'very_active')",
            name="ck_health_profile_activity",
        ),
        CheckConstraint(
            "goal IN ('lose', 'maintain', 'gain')",
            name="ck_health_profile_goal",
        ),
    )

    user_id: Mapped[int] = mapped_column(
        ForeignKey("users.id", ondelete="CASCADE"),
        primary_key=True,
    )
    birth_date: Mapped[date] = mapped_column(Date)
    sex: Mapped[str] = mapped_column(String(10))
    height_cm: Mapped[float] = mapped_column(Numeric(5, 2))
    weight_kg: Mapped[float] = mapped_column(Numeric(5, 2))
    activity_level: Mapped[str] = mapped_column(String(20))
    goal: Mapped[str] = mapped_column(String(20), default="maintain")
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=utc_now)
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), default=utc_now, onupdate=utc_now
    )
