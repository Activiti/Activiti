/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.api.runtime.shared.query;

public class Order {

    public enum Direction {
        ASC,

        DESC
    }

    private String property;

    private Direction direction;

    public Order(String property) {
        this(property, Direction.ASC);
    }

    private Order(String property,
                 Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public String getProperty() {
        return property;
    }

    public Direction getDirection() {
        return direction;
    }

    public static Order by(String property,
                     Direction direction) {
        return new Order(property, direction);
    }
}
