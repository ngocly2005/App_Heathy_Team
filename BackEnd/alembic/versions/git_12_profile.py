"""Compatibility update for the reconstructed task release, preserving old git_* revisions."""
from alembic import op
import sqlalchemy as sa
revision = 'git_12_profile'
down_revision = 'git_11_dual_auth'
branch_labels = None
depends_on = None

def upgrade():
    op.drop_constraint("ck_health_profile_activity", "health_profiles", type_="check")
    op.create_check_constraint("ck_health_profile_activity", "health_profiles", "activity_level IN ('sedentary', 'light', 'moderate', 'very_active', 'extra_active')")

def downgrade():
    op.execute("DO $$ BEGIN IF EXISTS (SELECT 1 FROM health_profiles WHERE activity_level = 'extra_active') THEN RAISE EXCEPTION 'Change extra_active before downgrade'; END IF; END $$;")
    op.drop_constraint("ck_health_profile_activity", "health_profiles", type_="check")
    op.create_check_constraint("ck_health_profile_activity", "health_profiles", "activity_level IN ('sedentary', 'light', 'moderate', 'very_active')")
