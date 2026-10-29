"""Water logs and preferences.

Reconstructed task sequence; not the original migration history.
"""

import sqlalchemy as sa
from alembic import op

revision = 'git_08_water'
down_revision = 'git_07_exercise'
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "water_logs",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("log_date", sa.Date(), nullable=False),
        sa.Column("amount_ml", sa.Integer(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint("amount_ml > 0", name="ck_water_log_amount"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
    )
    op.create_index(
        "ix_water_logs_user_date",
        "water_logs",
        ["user_id", "log_date"],
    )
    op.create_table(
        "water_settings",
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("target_ml", sa.Integer(), nullable=True),
        sa.Column("serving_ml", sa.Integer(), server_default="200", nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint(
            "target_ml IS NULL OR target_ml BETWEEN 1000 AND 4500",
            name="ck_water_settings_target",
        ),
        sa.CheckConstraint("serving_ml IN (200, 500)", name="ck_water_settings_serving"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("user_id"),
    )


def downgrade() -> None:
    op.drop_table("water_settings")
    op.drop_index("ix_water_logs_user_date", table_name="water_logs")
    op.drop_table("water_logs")
