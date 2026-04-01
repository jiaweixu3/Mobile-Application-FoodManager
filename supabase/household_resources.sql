create extension if not exists pgcrypto;

create unique index if not exists inventories_household_id_uidx
    on public.inventories (household_id);

create unique index if not exists shopping_lists_household_id_uidx
    on public.shopping_lists (household_id);

insert into public.inventories (household_id)
select h.id
from public.households h
left join public.inventories i on i.household_id = h.id
where i.id is null;

insert into public.shopping_lists (household_id)
select h.id
from public.households h
left join public.shopping_lists s on s.household_id = h.id
where s.id is null;

alter table public.inventories enable row level security;
alter table public.shopping_lists enable row level security;

drop policy if exists "inventories_select_household_members" on public.inventories;
create policy "inventories_select_household_members"
on public.inventories
for select
to authenticated
using (
    exists (
        select 1
        from public.household_members hm
        where hm.household_id = inventories.household_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "inventories_insert_household_members" on public.inventories;
create policy "inventories_insert_household_members"
on public.inventories
for insert
to authenticated
with check (
    exists (
        select 1
        from public.household_members hm
        where hm.household_id = inventories.household_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "shopping_lists_select_household_members" on public.shopping_lists;
create policy "shopping_lists_select_household_members"
on public.shopping_lists
for select
to authenticated
using (
    exists (
        select 1
        from public.household_members hm
        where hm.household_id = shopping_lists.household_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "shopping_lists_insert_household_members" on public.shopping_lists;
create policy "shopping_lists_insert_household_members"
on public.shopping_lists
for insert
to authenticated
with check (
    exists (
        select 1
        from public.household_members hm
        where hm.household_id = shopping_lists.household_id
          and hm.user_id = auth.uid()
    )
);
