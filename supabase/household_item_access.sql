alter table public.food_items enable row level security;
alter table public.shopping_items enable row level security;

drop policy if exists "food_items_select_household_members" on public.food_items;
create policy "food_items_select_household_members"
on public.food_items
for select
to authenticated
using (
    exists (
        select 1
        from public.inventories i
        join public.household_members hm on hm.household_id = i.household_id
        where i.id = food_items.inventory_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "food_items_insert_household_members" on public.food_items;
create policy "food_items_insert_household_members"
on public.food_items
for insert
to authenticated
with check (
    exists (
        select 1
        from public.inventories i
        join public.household_members hm on hm.household_id = i.household_id
        where i.id = food_items.inventory_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "food_items_update_household_members" on public.food_items;
create policy "food_items_update_household_members"
on public.food_items
for update
to authenticated
using (
    exists (
        select 1
        from public.inventories i
        join public.household_members hm on hm.household_id = i.household_id
        where i.id = food_items.inventory_id
          and hm.user_id = auth.uid()
    )
)
with check (
    exists (
        select 1
        from public.inventories i
        join public.household_members hm on hm.household_id = i.household_id
        where i.id = food_items.inventory_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "food_items_delete_household_members" on public.food_items;
create policy "food_items_delete_household_members"
on public.food_items
for delete
to authenticated
using (
    exists (
        select 1
        from public.inventories i
        join public.household_members hm on hm.household_id = i.household_id
        where i.id = food_items.inventory_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "shopping_items_select_household_members" on public.shopping_items;
create policy "shopping_items_select_household_members"
on public.shopping_items
for select
to authenticated
using (
    exists (
        select 1
        from public.shopping_lists sl
        join public.household_members hm on hm.household_id = sl.household_id
        where sl.id = shopping_items.shopping_list_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "shopping_items_insert_household_members" on public.shopping_items;
create policy "shopping_items_insert_household_members"
on public.shopping_items
for insert
to authenticated
with check (
    exists (
        select 1
        from public.shopping_lists sl
        join public.household_members hm on hm.household_id = sl.household_id
        where sl.id = shopping_items.shopping_list_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "shopping_items_update_household_members" on public.shopping_items;
create policy "shopping_items_update_household_members"
on public.shopping_items
for update
to authenticated
using (
    exists (
        select 1
        from public.shopping_lists sl
        join public.household_members hm on hm.household_id = sl.household_id
        where sl.id = shopping_items.shopping_list_id
          and hm.user_id = auth.uid()
    )
)
with check (
    exists (
        select 1
        from public.shopping_lists sl
        join public.household_members hm on hm.household_id = sl.household_id
        where sl.id = shopping_items.shopping_list_id
          and hm.user_id = auth.uid()
    )
);

drop policy if exists "shopping_items_delete_household_members" on public.shopping_items;
create policy "shopping_items_delete_household_members"
on public.shopping_items
for delete
to authenticated
using (
    exists (
        select 1
        from public.shopping_lists sl
        join public.household_members hm on hm.household_id = sl.household_id
        where sl.id = shopping_items.shopping_list_id
          and hm.user_id = auth.uid()
    )
);
