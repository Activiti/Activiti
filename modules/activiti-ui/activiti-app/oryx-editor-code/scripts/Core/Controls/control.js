/* Licensed under the Apache License, Version 2.0 (the "License");
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
 */

/**
 * Init namespaces
 */
if(!ORYX) {var ORYX = {};}
if(!ORYX.Core) {ORYX.Core = {};}
if(!ORYX.Core.Controls) {ORYX.Core.Controls = {};}


/**
 * @classDescription Abstract base class for all Controls.
 */
ORYX.Core.Controls.Control = ORYX.Core.UIObject.extend({
	
	toString: function() { return "Control " + this.id; }
 });