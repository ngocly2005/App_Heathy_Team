"""Compatibility update for the reconstructed task release, preserving old git_* revisions."""
from alembic import op
import sqlalchemy as sa
revision = 'git_14_ai_history'
down_revision = 'git_13_meal_time'
branch_labels = None
depends_on = None

def upgrade():
    op.create_table("ai_interactions",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), sa.ForeignKey("users.id", ondelete="CASCADE"), nullable=False),
        sa.Column("kind", sa.String(10), nullable=False),
        sa.Column("context_date", sa.Date(), nullable=False),
        sa.Column("question", sa.Text(), nullable=True),
        sa.Column("response", sa.JSON(), nullable=True),
        sa.Column("status", sa.String(10), nullable=False),
        sa.Column("error_code", sa.Integer(), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint("kind IN ('chat', 'photo')", name="ck_ai_kind"),
        sa.CheckConstraint("status IN ('success', 'error')", name="ck_ai_status"))
    op.create_index("ix_ai_user_created", "ai_interactions", ["user_id", "created_at"])

def downgrade():
    op.drop_index("ix_ai_user_created", table_name="ai_interactions")
    op.drop_table("ai_interactions")
