create extension if not exists pgcrypto;

create table if not exists public.household_members (
    id uuid primary key default gen_random_uuid(),
    household_id uuid not null references public.households(id) on delete cascade,
    user_id uuid not null references auth.users(id) on delete cascade,
    email text not null,
    display_name text not null,
    role text not null default 'Member' check (role in ('Owner', 'Member')),
    created_at timestamptz not null default now(),
    unique (household_id, user_id)
);

create index if not exists household_members_household_id_idx
    on public.household_members (household_id);

create index if not exists household_members_user_id_idx
    on public.household_members (user_id);

insert into public.household_members (household_id, user_id, email, display_name, role)
select
    h.id,
    h.created_by,
    coalesce(u.email, 'unknown@foodmanager.app'),
    coalesce(split_part(u.email, '@', 1), 'Owner'),
    'Owner'
from public.households h
left join auth.users u on u.id = h.created_by
where h.created_by is not null
on conflict (household_id, user_id) do nothing;

alter table public.household_members enable row level security;

create policy "household_members_select"
on public.household_members
for select
to authenticated
using (
    exists (
        select 1
        from public.household_members viewer
        where viewer.household_id = household_members.household_id
          and viewer.user_id = auth.uid()
    )
);

create policy "household_members_insert_self"
on public.household_members
for insert
to authenticated
with check (user_id = auth.uid());

create policy "household_members_delete_self_or_owner"
on public.household_members
for delete
to authenticated
using (
    user_id = auth.uid()
    or exists (
        select 1
        from public.household_members owner_row
        where owner_row.household_id = household_members.household_id
          and owner_row.user_id = auth.uid()
          and owner_row.role = 'Owner'
    )
);
