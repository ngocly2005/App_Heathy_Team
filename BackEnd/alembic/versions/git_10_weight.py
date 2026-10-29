"""Weight history and source profile backfill.

Reconstructed task sequence; not the original migration history.
"""

import sqlalchemy as sa
from alembic import op

revision = 'git_10_weight'
down_revision = 'git_09_saved_meals'
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "weight_entries",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("measured_on", sa.Date(), nullable=False),
        sa.Column("weight_kg", sa.Numeric(5, 2), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint("weight_kg > 0", name="ck_weight_entry_weight"),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.UniqueConstraint(
            "user_id",
            "measured_on",
            name="uq_weight_user_date",
        ),
    )
    op.create_index(
        "ix_weight_entries_user_id",
        "weight_entries",
        ["user_id"],
    )
    op.execute(
        """
        INSERT INTO weight_entries (user_id, measured_on, weight_kg, created_at)
        SELECT user_id, CURRENT_DATE, weight_kg, CURRENT_TIMESTAMP
        FROM health_profiles
        """
    )


def downgrade() -> None:
    op.drop_index("ix_weight_entries_user_id", table_name="weight_entries")
    op.drop_table("weight_entries")
