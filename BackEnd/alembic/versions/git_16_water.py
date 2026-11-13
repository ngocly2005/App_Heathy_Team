"""Compatibility update for the reconstructed task release, preserving old git_* revisions."""
from alembic import op
import sqlalchemy as sa
revision = 'git_16_water'
down_revision = 'git_15_nutrition'
branch_labels = None
depends_on = None

def upgrade():
    op.drop_constraint("ck_water_settings_target", "water_settings", type_="check")
    op.create_check_constraint("ck_water_settings_target", "water_settings", "target_ml IS NULL OR (target_ml >= 500 AND target_ml <= 10000)")
    op.drop_column("water_settings", "created_at")
    op.drop_column("water_settings", "updated_at")

def downgrade():
    op.execute("DO $$ BEGIN IF EXISTS (SELECT 1 FROM water_settings WHERE target_ml < 1000 OR target_ml > 4500) THEN RAISE EXCEPTION 'Adjust water targets before downgrade'; END IF; END $$;")
    op.add_column("water_settings", sa.Column("created_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()))
    op.add_column("water_settings", sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()))
    op.drop_constraint("ck_water_settings_target", "water_settings", type_="check")
    op.create_check_constraint("ck_water_settings_target", "water_settings", "target_ml IS NULL OR target_ml BETWEEN 1000 AND 4500")
