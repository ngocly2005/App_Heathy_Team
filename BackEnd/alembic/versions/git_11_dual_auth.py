"""Compatibility update for the reconstructed task release, preserving old git_* revisions."""
from alembic import op
import sqlalchemy as sa
revision = 'git_11_dual_auth'
down_revision = 'git_10_weight'
branch_labels = None
depends_on = None

def upgrade():
    op.add_column("users", sa.Column("role", sa.String(20), nullable=False, server_default="user"))
    op.create_check_constraint("ck_users_role", "users", "role IN ('user', 'admin')")
    op.add_column("users", sa.Column("password_hash", sa.String(255), nullable=True))
    op.alter_column("users", "google_sub", existing_type=sa.String(255), nullable=True)
    op.create_check_constraint("ck_users_auth_method", "users", "google_sub IS NOT NULL OR password_hash IS NOT NULL")

def downgrade():
    op.execute("DO $$ BEGIN IF EXISTS (SELECT 1 FROM users WHERE google_sub IS NULL) THEN RAISE EXCEPTION 'Link password-only users before downgrade'; END IF; END $$;")
    op.drop_constraint("ck_users_auth_method", "users", type_="check")
    op.alter_column("users", "google_sub", existing_type=sa.String(255), nullable=False)
    op.drop_column("users", "password_hash")
    op.drop_constraint("ck_users_role", "users", type_="check")
    op.drop_column("users", "role")
