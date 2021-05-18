/*
 * Copyright 2019 Björn Kautler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.celestialdata.plexbot.discord.commandhandler.restriction;

import net.celestialdata.plexbot.discord.commandhandler.api.restriction.Restriction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Comparator.comparingInt;

/**
 * A directory of restrictions that can be looked up by their type.
 *
 * @param <M> the class of the messages this lookup can provide
 */
public class RestrictionLookup<M> {
    /**
     * The restrictions.
     */
    private final Set<Restriction<? super M>> restrictions = new CopyOnWriteArraySet<>();

    /**
     * The restrictions by class. As the actual restriction instances are proxied by CDI, this map cannot be
     * built automatically from the available restrictions, but only on the fly.
     */
    private final Map<Class<?>, Restriction<? super M>> restrictionByClass = new ConcurrentHashMap<>();

    private static int getInheritanceDistance(Restriction<?> restriction, Class<?> restrictionClass) {
        int distance = 0;
        for (Class<?> clazz = restriction.getClass();
             !clazz.equals(restrictionClass) && restrictionClass.isAssignableFrom(clazz);
             clazz = clazz.getSuperclass()) {
            distance++;
        }
        return distance;
    }

    /**
     * Adds the given restrictions to the set of available restrictions in this lookup.
     *
     * @param restrictions the restrictions to add
     */
    public void addAllRestrictions(Collection<Restriction<? super M>> restrictions) {
        this.restrictions.addAll(restrictions);
        restrictionByClass.clear();
    }

    /**
     * Returns the restriction instance that fits to the given class or {@code null}.
     *
     * @param restrictionClass the restriction class to look up.
     * @return the restriction instance that fits to the given class or {@code null}
     */
    public Restriction<? super M> getRestriction(Class<?> restrictionClass) {
        return restrictionByClass
                .computeIfAbsent(restrictionClass,
                        key -> restrictions.stream()
                                // we cannot use a map as the classes are proxied by CDI
                                .filter(key::isInstance)
                                .min(comparingInt(restriction -> getInheritanceDistance(restriction, key)))
                                .orElse(null));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RestrictionLookup.class.getSimpleName() + "[", "]")
                .add("restrictions=" + restrictions)
                .add("restrictionByClass=" + restrictionByClass)
                .toString();
    }
}
