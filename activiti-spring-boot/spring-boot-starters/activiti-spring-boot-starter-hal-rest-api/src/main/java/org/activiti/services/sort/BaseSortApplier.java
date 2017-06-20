/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.services.sort;

import org.activiti.engine.query.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**

 */
public abstract class BaseSortApplier<T extends Query<?, ?>> implements SortApplier<T> {

    public void applySort(T query, Pageable pageable) {
        if (pageable.getSort() != null) {
            applyPageableSort(query, pageable.getSort());
        } else {
            applyDefaultSort(query);
        }
    }

    protected abstract void applyDefaultSort(T query);

    private void applyPageableSort(T query, Sort sort) {
        for (Sort.Order order : sort) {
            applyOrder(query, order);
            applyDirection(query, order.getDirection());
        }

    }

    protected abstract void applyOrder(T query, Sort.Order order);

    private void applyDirection(T query, Sort.Direction direction) {
        switch (direction) {

            case ASC:
                query.asc();
                break;

            case DESC:
                query.desc();
                break;
        }
    }

}
