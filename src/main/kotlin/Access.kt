/*
 *	Copyright 2022 cufy.org
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	    http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */
package org.cufy.openperm

/**
 * A marker interface for objects that is used as
 * access specifiers.
 *
 * @author LSafer
 * @since 1.3.0
 */
interface Access {
    /**
     * Marker interface for operation-based access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    interface Operation : Access {
        /**
         * Marker interface for read access.
         *
         * @author LSafer
         * @since 1.3.0
         */
        interface Read : Operation {
            /**
             * Access marker for read access.
             *
             * @author LSafer
             * @since 1.3.0
             */
            companion object : Read
        }

        /**
         * Marker interface for write-operation access.
         *
         * @author LSafer
         * @since 1.3.0
         */
        interface Write : Operation {
            /**
             * Access marker for write access.
             *
             * @author LSafer
             * @since 1.3.0
             */
            companion object : Write
        }
    }

    /**
     * Marker interface for level-based access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    interface Level : Access {
        /**
         * Marker interface for anonymous-level access.
         *
         * @author LSafer
         * @since 1.3.0
         */
        interface Anonymous : Level {
            /**
             * Access marker for anonymous-level access.
             *
             * @author LSafer
             * @since 1.3.0
             */
            companion object : Anonymous
        }

        /**
         * Marker interface for default-level access.
         *
         * @author LSafer
         * @since 1.3.0
         */
        interface Default : Level {
            /**
             * Access marker for default-level access.
             *
             * @author LSafer
             * @since 1.3.0
             */
            companion object : Default
        }

        /**
         * Marker interface for owner-level access.
         *
         * @author LSafer
         * @since 1.3.0
         */
        interface Owner : Level {
            /**
             * Access marker for owner-level access.
             *
             * @author LSafer
             * @since 1.3.0
             */
            companion object : Owner
        }
    }

    /**
     * Access marker for anonymous-level read access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    object AnonymousRead : Operation.Read, Level.Anonymous

    /**
     * Access marker for anonymous-level write access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    object AnonymousWrite : Operation.Write, Level.Anonymous

    /**
     * Access marker for default-level read access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    object Read : Operation.Read, Level.Default

    /**
     * Access marker for default-level write access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    object Write : Operation.Write, Level.Default

    /**
     * Access marker for owner-level read access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    object OwnerRead : Operation.Read, Level.Owner

    /**
     * Access marker for owner-level write access.
     *
     * @author LSafer
     * @since 1.3.0
     */
    object OwnerWrite : Operation.Write, Level.Owner
}
