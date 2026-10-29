"""Exercise types and activity logs.

Reconstructed task sequence; not the original migration history.
"""

import sqlalchemy as sa
from alembic import op

revision = 'git_07_exercise'
down_revision = 'git_04_seed_foods'
branch_labels = None
depends_on = None


def upgrade() -> None:
    activity_types = op.create_table(
        "activity_types",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("name", sa.String(length=120), nullable=False),
        sa.Column("met", sa.Numeric(5, 2), nullable=False),
        sa.Column("is_active", sa.Boolean(), nullable=False),
        sa.CheckConstraint("met > 0", name="ck_activity_type_met"),
        sa.UniqueConstraint("name"),
    )
    op.create_index("ix_activity_types_is_active", "activity_types", ["is_active"])
    op.bulk_insert(
        activity_types,
        [
            {"name": "Walking", "met": 3.5, "is_active": True},
            {"name": "Running", "met": 8.3, "is_active": True},
            {"name": "Cycling", "met": 7.5, "is_active": True},
            {"name": "Swimming", "met": 6.0, "is_active": True},
        ],
    )
    op.create_table(
        "activity_logs",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("activity_type_id", sa.Integer(), nullable=True),
        sa.Column("log_date", sa.Date(), nullable=False),
        sa.Column("duration_minutes", sa.Integer(), nullable=False),
        sa.Column("activity_name", sa.String(length=120), nullable=False),
        sa.Column("met_snapshot", sa.Numeric(5, 2), nullable=False),
        sa.Column("calories_burned", sa.Numeric(9, 2), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "duration_minutes > 0",
            name="ck_activity_log_duration",
        ),
        sa.CheckConstraint("met_snapshot > 0", name="ck_activity_log_met"),
        sa.CheckConstraint(
            "calories_burned >= 0",
            name="ck_activity_log_calories",
        ),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(
            ["activity_type_id"],
            ["activity_types.id"],
            ondelete="SET NULL",
        ),
    )
    op.create_index(
        "ix_activity_logs_user_date",
        "activity_logs",
        ["user_id", "log_date"],
    )


def downgrade() -> None:
    op.drop_index("ix_activity_logs_user_date", table_name="activity_logs")
    op.drop_table("activity_logs")
    op.drop_index("ix_activity_types_is_active", table_name="activity_types")
    op.drop_table("activity_types")
