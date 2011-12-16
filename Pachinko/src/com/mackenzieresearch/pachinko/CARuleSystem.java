/*
   PACHINKO, A fast, embeddable micro-rule engine runtime.
   Version 0.2

   Copyright 2011 Kenneth R. Mackenzie (www.mackenzieresearch.com)

   This program is free software: you can redistribute it and/or modify
   it under the terms of Version 3 of the GNU Affero General Public
   License as published by the Free Software Foundation.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   If you require a version of this software which can be used as
   part of a commercial for-profit program, please contact Mackenzie
   Research for a commercial license.

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.

   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.mackenzieresearch.pachinko;

public class CARuleSystem<P> extends RuleSystemCore<P, ICARule<P>> {

  public CARuleSystem(ICARule<P>... rules) {
    _rules = rules;
    _assembleActivationContexts();
  }


  public void executeActivations() {
    while (!_activationQueue.isEmpty()) {
      ActivationContext<P, ICARule<P>> betaMemory = _activationQueue.remove();
//      betaMemory.resetKeyFieldRefCount();
      betaMemory.getRule().evaluate(betaMemory);
    }
  }
}
