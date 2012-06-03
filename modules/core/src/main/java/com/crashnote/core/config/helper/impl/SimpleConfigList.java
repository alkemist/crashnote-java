/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package com.crashnote.core.config.helper.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.crashnote.core.config.helper.ConfigException;
import com.crashnote.core.config.helper.ConfigList;
import com.crashnote.core.config.helper.ConfigOrigin;
import com.crashnote.core.config.helper.ConfigRenderOptions;
import com.crashnote.core.config.helper.ConfigValue;
import com.crashnote.core.config.helper.ConfigValueType;

final class SimpleConfigList extends AbstractConfigValue implements ConfigList, Serializable {

    private static final long serialVersionUID = 2L;

    final private List<AbstractConfigValue> value;
    final private boolean resolved;

    SimpleConfigList(final ConfigOrigin origin, final List<AbstractConfigValue> value) {
        this(origin, value, ResolveStatus
                .fromValues(value));
    }

    SimpleConfigList(final ConfigOrigin origin, final List<AbstractConfigValue> value,
            final ResolveStatus status) {
        super(origin);
        this.value = value;
        this.resolved = status == ResolveStatus.RESOLVED;

        // kind of an expensive debug check (makes this constructor pointless)
        if (status != ResolveStatus.fromValues(value))
            throw new ConfigException.BugOrBroken(
                    "SimpleConfigList created with wrong resolve status: " + this);
    }

    @Override
    public ConfigValueType valueType() {
        return ConfigValueType.LIST;
    }

    @Override
    public List<Object> unwrapped() {
        final List<Object> list = new ArrayList<Object>();
        for (final AbstractConfigValue v : value) {
            list.add(v.unwrapped());
        }
        return list;
    }

    @Override
    ResolveStatus resolveStatus() {
        return ResolveStatus.fromBoolean(resolved);
    }

    private SimpleConfigList modify(final NoExceptionsModifier modifier, final ResolveStatus newResolveStatus) {
        try {
            return modifyMayThrow(modifier, newResolveStatus);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException.BugOrBroken("unexpected checked exception", e);
        }
    }

    private SimpleConfigList modifyMayThrow(final Modifier modifier, final ResolveStatus newResolveStatus)
            throws Exception {
        // lazy-create for optimization
        List<AbstractConfigValue> changed = null;
        int i = 0;
        for (final AbstractConfigValue v : value) {
            final AbstractConfigValue modified = modifier.modifyChildMayThrow(null /* key */, v);

            // lazy-create the new list if required
            if (changed == null && modified != v) {
                changed = new ArrayList<AbstractConfigValue>();
                for (int j = 0; j < i; ++j) {
                    changed.add(value.get(j));
                }
            }

            // once the new list is created, all elements
            // have to go in it. if modifyChild returned
            // null, we drop that element.
            if (changed != null && modified != null) {
                changed.add(modified);
            }

            i += 1;
        }

        if (changed != null) {
            return new SimpleConfigList(origin(), changed, newResolveStatus);
        } else {
            return this;
        }
    }

    @Override
    SimpleConfigList resolveSubstitutions(final ResolveContext context) throws NotPossibleToResolve {
        if (resolved)
            return this;

        if (context.isRestrictedToChild()) {
            // if a list restricts to a child path, then it has no child paths,
            // so nothing to do.
            return this;
        } else {
            try {
                return modifyMayThrow(new Modifier() {
                    @Override
                    public AbstractConfigValue modifyChildMayThrow(final String key, final AbstractConfigValue v)
                            throws NotPossibleToResolve {
                        return context.resolve(v);
                    }

                }, ResolveStatus.RESOLVED);
            } catch (NotPossibleToResolve e) {
                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ConfigException.BugOrBroken("unexpected checked exception", e);
            }
        }
    }

    @Override
    SimpleConfigList relativized(final Path prefix) {
        return modify(new NoExceptionsModifier() {
            @Override
            public AbstractConfigValue modifyChild(final String key, final AbstractConfigValue v) {
                return v.relativized(prefix);
            }

        }, resolveStatus());
    }

    @Override
    protected boolean canEqual(final Object other) {
        return other instanceof SimpleConfigList;
    }

    @Override
    public boolean equals(final Object other) {
        // note that "origin" is deliberately NOT part of equality
        if (other instanceof SimpleConfigList) {
            // optimization to avoid unwrapped() for two ConfigList
            return canEqual(other) && value.equals(((SimpleConfigList) other).value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // note that "origin" is deliberately NOT part of equality
        return value.hashCode();
    }

    @Override
    protected void render(final StringBuilder sb, final int indent, final ConfigRenderOptions options) {
        if (value.isEmpty()) {
            sb.append("[]");
        } else {
            sb.append("[");
            if (options.getFormatted())
                sb.append('\n');
            for (final AbstractConfigValue v : value) {
                if (options.getOriginComments()) {
                    indent(sb, indent + 1, options);
                    sb.append("# ");
                    sb.append(v.origin().description());
                    sb.append("\n");
                }
                if (options.getComments()) {
                    for (final String comment : v.origin().comments()) {
                        indent(sb, indent + 1, options);
                        sb.append("# ");
                        sb.append(comment);
                        sb.append("\n");
                    }
                }
                indent(sb, indent + 1, options);

                v.render(sb, indent + 1, options);
                sb.append(",");
                if (options.getFormatted())
                    sb.append('\n');
            }
            sb.setLength(sb.length() - 1); // chop or newline
            if (options.getFormatted()) {
                sb.setLength(sb.length() - 1); // also chop comma
                sb.append('\n');
                indent(sb, indent, options);
            }
            sb.append("]");
        }
    }

    @Override
    public boolean contains(final Object o) {
        return value.contains(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return value.containsAll(c);
    }

    @Override
    public AbstractConfigValue get(final int index) {
        return value.get(index);
    }

    @Override
    public int indexOf(final Object o) {
        return value.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public Iterator<ConfigValue> iterator() {
        final Iterator<AbstractConfigValue> i = value.iterator();

        return new Iterator<ConfigValue>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public ConfigValue next() {
                return i.next();
            }

            @Override
            public void remove() {
                throw weAreImmutable("iterator().remove");
            }
        };
    }

    @Override
    public int lastIndexOf(final Object o) {
        return value.lastIndexOf(o);
    }

    private static ListIterator<ConfigValue> wrapListIterator(
            final ListIterator<AbstractConfigValue> i) {
        return new ListIterator<ConfigValue>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public ConfigValue next() {
                return i.next();
            }

            @Override
            public void remove() {
                throw weAreImmutable("listIterator().remove");
            }

            @Override
            public void add(final ConfigValue arg0) {
                throw weAreImmutable("listIterator().add");
            }

            @Override
            public boolean hasPrevious() {
                return i.hasPrevious();
            }

            @Override
            public int nextIndex() {
                return i.nextIndex();
            }

            @Override
            public ConfigValue previous() {
                return i.previous();
            }

            @Override
            public int previousIndex() {
                return i.previousIndex();
            }

            @Override
            public void set(final ConfigValue arg0) {
                throw weAreImmutable("listIterator().set");
            }
        };
    }

    @Override
    public ListIterator<ConfigValue> listIterator() {
        return wrapListIterator(value.listIterator());
    }

    @Override
    public ListIterator<ConfigValue> listIterator(final int index) {
        return wrapListIterator(value.listIterator(index));
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public List<ConfigValue> subList(final int fromIndex, final int toIndex) {
        final List<ConfigValue> list = new ArrayList<ConfigValue>();
        // yay bloat caused by lack of type variance
        for (final AbstractConfigValue v : value.subList(fromIndex, toIndex)) {
            list.add(v);
        }
        return list;
    }

    @Override
    public Object[] toArray() {
        return value.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return value.toArray(a);
    }

    private static UnsupportedOperationException weAreImmutable(final String method) {
        return new UnsupportedOperationException(
                "ConfigList is immutable, you can't call List.'" + method + "'");
    }

    @Override
    public boolean add(final ConfigValue e) {
        throw weAreImmutable("add");
    }

    @Override
    public void add(final int index, final ConfigValue element) {
        throw weAreImmutable("add");
    }

    @Override
    public boolean addAll(final Collection<? extends ConfigValue> c) {
        throw weAreImmutable("addAll");
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends ConfigValue> c) {
        throw weAreImmutable("addAll");
    }

    @Override
    public void clear() {
        throw weAreImmutable("clear");
    }

    @Override
    public boolean remove(final Object o) {
        throw weAreImmutable("remove");
    }

    @Override
    public ConfigValue remove(final int index) {
        throw weAreImmutable("remove");
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw weAreImmutable("removeAll");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw weAreImmutable("retainAll");
    }

    @Override
    public ConfigValue set(final int index, final ConfigValue element) {
        throw weAreImmutable("set");
    }

    @Override
    protected SimpleConfigList newCopy(final ConfigOrigin newOrigin) {
        return new SimpleConfigList(newOrigin, value);
    }

    final SimpleConfigList concatenate(final SimpleConfigList other) {
        final ConfigOrigin combinedOrigin = SimpleConfigOrigin.mergeOrigins(origin(), other.origin());
        final List<AbstractConfigValue> combined = new ArrayList<AbstractConfigValue>(value.size()
                + other.value.size());
        combined.addAll(value);
        combined.addAll(other.value);
        return new SimpleConfigList(combinedOrigin, combined);
    }

    // serialization all goes through SerializedConfigValue
    private Object writeReplace() throws ObjectStreamException {
        return new SerializedConfigValue(this);
    }
}