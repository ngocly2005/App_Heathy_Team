"""Existing source demo food catalog rows.

Reconstructed task sequence; not the original migration history.
"""

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects.postgresql import insert

revision = 'git_04_seed_foods'
down_revision = 'git_02_food_logs'
branch_labels = None
depends_on = None


def upgrade() -> None:
    foods = sa.table(
        "foods",
        sa.column("name", sa.String()),
        sa.column("calories_per_100g", sa.Numeric()),
        sa.column("protein_per_100g", sa.Numeric()),
        sa.column("carb_per_100g", sa.Numeric()),
        sa.column("fat_per_100g", sa.Numeric()),
        sa.column("is_active", sa.Boolean()),
    )
    for row in [{'name': 'Cooked chicken breast',
      'calories_per_100g': 165,
      'protein_per_100g': 31,
      'carb_per_100g': 0,
      'fat_per_100g': 3.6,
      'is_active': True},
     {'name': 'Cooked white rice',
      'calories_per_100g': 130,
      'protein_per_100g': 2.7,
      'carb_per_100g': 28.2,
      'fat_per_100g': 0.3,
      'is_active': True},
     {'name': 'Boiled egg',
      'calories_per_100g': 155,
      'protein_per_100g': 12.6,
      'carb_per_100g': 1.1,
      'fat_per_100g': 10.6,
      'is_active': True},
     {'name': 'Banana',
      'calories_per_100g': 89,
      'protein_per_100g': 1.1,
      'carb_per_100g': 22.8,
      'fat_per_100g': 0.3,
      'is_active': True},
     {'name': 'Apple',
      'calories_per_100g': 52,
      'protein_per_100g': 0.3,
      'carb_per_100g': 13.8,
      'fat_per_100g': 0.2,
      'is_active': True}]:
        op.execute(
            insert(foods).values(**row).on_conflict_do_nothing(index_elements=["name"])
        )


def downgrade() -> None:
    # Preserve seed rows and any logs referencing them on downgrade.
    pass
