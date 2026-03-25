create extension if not exists pgcrypto;

create table if not exists public.favorite_items (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id) on delete cascade,
    name text not null,
    amount double precision not null check (amount > 0),
    unit text not null,
    category text not null,
    created_at timestamptz not null default now()
);

create index if not exists favorite_items_household_id_idx
    on public.favorite_items (household_id);

create unique index if not exists favorite_items_household_name_unit_category_uidx
    on public.favorite_items (household_id, lower(name), lower(unit), lower(category));

alter table public.favorite_items enable row level security;

create policy "favorite_items_select_authenticated"
on public.favorite_items
for select
to authenticated
using (true);

create policy "favorite_items_insert_authenticated"
on public.favorite_items
for insert
to authenticated
with check (true);

create policy "favorite_items_delete_authenticated"
on public.favorite_items
for delete
to authenticated
using (true);
