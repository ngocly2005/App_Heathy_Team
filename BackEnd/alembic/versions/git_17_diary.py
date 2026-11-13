"""Compatibility update for the reconstructed task release, preserving old git_* revisions."""
from alembic import op
import sqlalchemy as sa
revision = 'git_17_diary'
down_revision = 'git_16_water'
branch_labels = None
depends_on = None

def upgrade():
    op.create_table(
        "diary_entries",
        sa.Column("id", sa.Integer(), primary_key=True),
        sa.Column("user_id", sa.Integer(), nullable=False),
        sa.Column("entry_date", sa.Date(), nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column("mood", sa.String(length=30), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["user_id"], ["users.id"], ondelete="CASCADE"),
        sa.UniqueConstraint("user_id", "entry_date", name="uq_diary_user_date"),
    )
    op.create_index(
        "ix_diary_entries_user_id",
        "diary_entries",
        ["user_id"],
    )

def downgrade():
    op.drop_index("ix_diary_entries_user_id", table_name="diary_entries")
    op.drop_table("diary_entries")
