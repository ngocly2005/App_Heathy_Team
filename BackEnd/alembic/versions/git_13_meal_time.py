"""Compatibility update for the reconstructed task release, preserving old git_* revisions."""
from alembic import op
import sqlalchemy as sa
revision = 'git_13_meal_time'
down_revision = 'git_12_profile'
branch_labels = None
depends_on = None

def upgrade():
    op.add_column("food_logs", sa.Column("meal_time", sa.String(5), nullable=True))

def downgrade():
    op.drop_column("food_logs", "meal_time")
