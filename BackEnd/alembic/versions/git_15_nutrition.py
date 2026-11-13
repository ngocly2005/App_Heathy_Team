"""Compatibility update for the reconstructed task release, preserving old git_* revisions."""
from alembic import op
import sqlalchemy as sa
revision = 'git_15_nutrition'
down_revision = 'git_14_ai_history'
branch_labels = None
depends_on = None

def upgrade():
    op.create_table("nutrition_settings",
        sa.Column("user_id", sa.Integer(), sa.ForeignKey("users.id", ondelete="CASCADE"), primary_key=True),
        sa.Column("calorie_target", sa.Numeric(8, 2), nullable=True),
        sa.Column("protein_pct", sa.Integer(), nullable=False, server_default="25"),
        sa.Column("carb_pct", sa.Integer(), nullable=False, server_default="50"),
        sa.Column("fat_pct", sa.Integer(), nullable=False, server_default="25"),
        sa.Column("target_weight_kg", sa.Numeric(5, 2), nullable=True),
        sa.CheckConstraint("calorie_target IS NULL OR (calorie_target >= 500 AND calorie_target <= 10000)", name="ck_nutrition_calorie"),
        sa.CheckConstraint("protein_pct >= 0 AND carb_pct >= 0 AND fat_pct >= 0 AND protein_pct + carb_pct + fat_pct = 100", name="ck_nutrition_macro"),
        sa.CheckConstraint("target_weight_kg IS NULL OR (target_weight_kg > 20 AND target_weight_kg <= 400)", name="ck_nutrition_weight"))

def downgrade():
    op.drop_table("nutrition_settings")
