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

import com.mackenzieresearch.roux.kernel.*;

import static util.RuntimeAssertion.assertNotNull;

public class CAExpressionRule<P> extends DefaultCARule<P> {
  protected ISideEffectFreeExpression<P, Boolean> _condition = null;
  protected ISideEffectFreeAction<P> _action = null;

  public CAExpressionRule() {
    super();
  }

  public CAExpressionRule(ISideEffectFreeExpression<P, Boolean> condition, ISideEffectFreeAction<P> action) {
    super();
    whereCondition(condition);
    performAction(action);
  }

  public CAExpressionRule whereCondition(ISideEffectFreeExpression<P, Boolean> condition) {
    assertNotNull(condition);
    _condition = condition;

    //  Add freevars for condition expression...
    IReadWriteMonadex<IMonad<P>> conditionVars = _condition.freeVariables();
    for (IMonad<P> var : conditionVars.monads())
      addVariable(var);

    return this;
  }

  public CAExpressionRule performAction(ISideEffectFreeAction<P> action) {
    assertNotNull(action);
    _action = action;

    //  Add freevars for action expression...
    IReadWriteMonadex<IMonad<P>> actionVars = _action.freeVariables();
    for (IMonad<P> var : actionVars.monads())
      addVariable(var);

    return this;
  }

  @Override
  public boolean evaluateCondition(IMonadex context) {
    return _condition.evaluate(context);
  }

  @Override
  public void doAction(IReadWriteMonadex context) {
    _action.evaluate(context);
  }
}
