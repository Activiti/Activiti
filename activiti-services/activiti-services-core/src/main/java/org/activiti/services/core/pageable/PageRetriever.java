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

package org.activiti.services.core.pageable;

import java.util.List;

import org.activiti.engine.query.Query;
import org.activiti.services.core.model.converter.ModelConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class PageRetriever {

    public <SOURCE, TARGET> Page<TARGET> loadPage(Query<?, SOURCE> query, Pageable pageable, ModelConverter<SOURCE, TARGET> converter) {
        List<SOURCE> elements = query.listPage(Math.toIntExact(pageable.getOffset()), pageable.getPageSize());
        long count = query.count();
        return new PageImpl<>(converter.from(elements), pageable, count);
    }

}
