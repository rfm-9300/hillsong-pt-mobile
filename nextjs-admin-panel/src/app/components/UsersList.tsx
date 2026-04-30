'use client';

import { useMemo, useState } from 'react';
import { User } from '@/lib/types';
import { getUserDisplayName, isUserAdmin } from '@/lib/userUtils';
import { Badge, Button, Card, EmptyState, Input, LoadingOverlay, Select, TableHeader, TableRow } from './ui';
import { EditIcon, GridIcon, SearchIcon, TableIcon, TrashIcon, UsersIcon } from './icons/Icons';

interface UsersListProps {
  users: User[];
  loading?: boolean;
  error?: string | null;
  onEdit?: (user: User) => void;
  onDelete?: (user: User) => void;
}

type RoleFilter = 'all' | 'admin' | 'user';
type VerificationFilter = 'all' | 'verified' | 'unverified';
type ViewMode = 'grid' | 'table';

const cols = [
  { label: 'Name', width: '220px' },
  { label: 'Email', width: '1fr' },
  { label: 'Role', width: '100px' },
  { label: 'Status', width: '110px' },
  { label: 'Joined', width: '120px' },
  { label: '', width: '100px' },
];

export default function UsersList({ users, loading = false, error, onEdit, onDelete }: UsersListProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<RoleFilter>('all');
  const [verificationFilter, setVerificationFilter] = useState<VerificationFilter>('all');
  const [viewMode, setViewMode] = useState<ViewMode>('grid');

  const filteredUsers = useMemo(() => {
    const query = searchQuery.toLowerCase();
    return users.filter((user) => {
      const matchesSearch = getUserDisplayName(user).toLowerCase().includes(query) || user.email.toLowerCase().includes(query);
      const matchesRole = roleFilter === 'all' || (roleFilter === 'admin' && isUserAdmin(user)) || (roleFilter === 'user' && !isUserAdmin(user));
      const matchesVerification = verificationFilter === 'all' || (verificationFilter === 'verified' && user.verified) || (verificationFilter === 'unverified' && !user.verified);
      return matchesSearch && matchesRole && matchesVerification;
    });
  }, [users, searchQuery, roleFilter, verificationFilter]);

  const hasFilters = Boolean(searchQuery || roleFilter !== 'all' || verificationFilter !== 'all');
  const clearFilters = () => {
    setSearchQuery('');
    setRoleFilter('all');
    setVerificationFilter('all');
  };

  if (error) {
    return <Card className="p-4 text-[13px] text-[var(--color-danger)]">{error}</Card>;
  }

  return (
    <div className="space-y-4">
      <Card className="p-4">
        <div className="grid grid-cols-1 items-end gap-2.5 sm:grid-cols-2 lg:grid-cols-[1fr_160px_180px_auto_auto]">
          <Input placeholder="Search by name or email..." value={searchQuery} onChange={setSearchQuery} prefix={<SearchIcon />} />
          <Select value={roleFilter} onChange={(event) => setRoleFilter(event.target.value as RoleFilter)}>
            <option value="all">All Roles</option>
            <option value="admin">Admins</option>
            <option value="user">Users</option>
          </Select>
          <Select value={verificationFilter} onChange={(event) => setVerificationFilter(event.target.value as VerificationFilter)}>
            <option value="all">All Status</option>
            <option value="verified">Verified</option>
            <option value="unverified">Unverified</option>
          </Select>
          {hasFilters ? <Button variant="ghost" size="sm" onClick={clearFilters}>Clear</Button> : <div className="hidden lg:block" />}
          <div className="hidden overflow-hidden rounded-[7px] border border-[var(--color-border-med)] sm:flex">
            <ViewButton active={viewMode === 'grid'} onClick={() => setViewMode('grid')}><GridIcon /></ViewButton>
            <ViewButton active={viewMode === 'table'} onClick={() => setViewMode('table')}><TableIcon /></ViewButton>
          </div>
        </div>
        <div className="mt-3 text-[12px] text-[var(--color-text-sub)]">
          Showing {filteredUsers.length} of {users.length}
          {hasFilters && <button type="button" onClick={clearFilters} className="ml-2 font-semibold text-[var(--color-accent)]">Clear filters</button>}
        </div>
      </Card>

      <LoadingOverlay show={loading} message="Loading users..." />

      {!loading && filteredUsers.length === 0 && (
        <Card>
          <EmptyState title="No users found" description="Try adjusting your search or filter criteria." actionText={hasFilters ? 'Clear filters' : undefined} onAction={hasFilters ? clearFilters : undefined} icon={<UsersIcon />} />
        </Card>
      )}

      {!loading && filteredUsers.length > 0 && viewMode === 'table' && (
        <Card className="overflow-hidden">
          <TableHeader cols={cols} />
          {filteredUsers.map((user, index) => (
            <TableRow
              key={user.id}
              cols={cols}
              last={index === filteredUsers.length - 1}
              cells={[
                <UserIdentity key="name" user={user} />,
                <span key="email" className="truncate">{user.email}</span>,
                <Badge key="role" color={isUserAdmin(user) ? 'amber' : 'neutral'}>{isUserAdmin(user) ? 'Admin' : 'User'}</Badge>,
                <Badge key="verified" color={user.verified ? 'green' : 'yellow'}>{user.verified ? 'Verified' : 'Unverified'}</Badge>,
                <span key="joined">{formatDate(user.joinedAt)}</span>,
              ]}
              actions={
                <div className="flex flex-wrap items-center gap-1.5">
                  <Button size="xs" variant="ghost" icon={<EditIcon />} onClick={() => onEdit?.(user)}>Edit</Button>
                  <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => onDelete?.(user)}>Del</Button>
                </div>
              }
            />
          ))}
        </Card>
      )}

      {!loading && filteredUsers.length > 0 && viewMode === 'grid' && (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {filteredUsers.map((user) => (
            <Card key={user.id} className="p-3 text-center sm:p-4">
              <div className="mx-auto mb-2 flex h-11 w-11 items-center justify-center rounded-full border border-[rgba(201,149,42,0.45)] bg-[var(--color-accent-sub)] font-display text-[18px] text-[var(--color-accent)]">
                {getUserDisplayName(user).charAt(0).toUpperCase()}
              </div>
              <h3 className="truncate text-[13px] font-bold text-[var(--color-text)]">{getUserDisplayName(user)}</h3>
              <p className="mb-3 truncate text-[11px] text-[var(--color-text-sub)]">{user.email}</p>
              <div className="mb-3 flex justify-center gap-1.5">
                <Badge color={isUserAdmin(user) ? 'amber' : 'neutral'}>{isUserAdmin(user) ? 'Admin' : 'User'}</Badge>
                <Badge color={user.verified ? 'green' : 'yellow'}>{user.verified ? 'Verified' : 'Unverified'}</Badge>
              </div>
              <div className="flex flex-wrap justify-center gap-1.5">
                <Button size="xs" variant="ghost" icon={<EditIcon />} onClick={() => onEdit?.(user)}>Edit</Button>
                <Button size="xs" variant="danger" icon={<TrashIcon />} onClick={() => onDelete?.(user)}>Del</Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function ViewButton({ active, onClick, children }: { active: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button type="button" onClick={onClick} className={`flex h-8 w-8 items-center justify-center transition-colors ${active ? 'bg-[var(--color-accent-sub)] text-[var(--color-accent)]' : 'text-[var(--color-text-sub)] hover:bg-[var(--color-surface-alt)]'}`}>
      {children}
    </button>
  );
}

function UserIdentity({ user }: { user: User }) {
  const name = getUserDisplayName(user);
  return (
    <div className="flex min-w-0 items-center gap-2">
      <div className="flex h-[26px] w-[26px] shrink-0 items-center justify-center rounded-full border border-[rgba(201,149,42,0.45)] bg-[var(--color-accent-sub)] font-display text-[12px] text-[var(--color-accent)]">
        {name.charAt(0).toUpperCase()}
      </div>
      <span className="truncate font-semibold text-[var(--color-text)]">{name}</span>
    </div>
  );
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : '-';
}
