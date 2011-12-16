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

import com.mackenzieresearch.roux.context.Monadex;
import com.mackenzieresearch.roux.kernel.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class DefaultCARule<P> implements ICARule<P> {
  private ActivationContext<P, ICARule<P>> _ruleMemory = null;

  protected DefaultCARule() {
    _ruleMemory = new ActivationContext<P, ICARule<P>>(this);
  }

  public abstract boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context);

  public abstract void doAction(IReadWriteMonadex<? extends IMonad> context);

  @Override
  public void evaluate(IReadWriteMonadex context) {
    if (evaluateCondition(context))
      doAction(context);
  }

  protected int addVariable(IMonad variable) {
    _ruleMemory.defineVariableBinding(variable);
    return _ruleMemory.getIndex(variable.getName());
  }

  protected int addVariable(IMonad variable, Object activationValue) {
    _ruleMemory.defineVariableBinding(variable, activationValue);
    return _ruleMemory.getIndex(variable.getName());
  }

  protected int addOptionalVariable(IMonad variable) {
    _ruleMemory.defineOptionalVariableBinding(variable);
    return _ruleMemory.getIndex(variable.getName());
  }

  protected int addPkVariable(IMonad variable) {
    _ruleMemory.definePKVariableBinding(variable);
    return _ruleMemory.getIndex(variable.getName());
  }

  protected int addPkVariable(IMonad variable, Object activationValue) {
    _ruleMemory.definePKVariableBinding(variable, activationValue);
    return _ruleMemory.getIndex(variable.getName());
  }

  @Override
  public IReadWriteMonadex freeVariables() {
    return _ruleMemory;
  }

  @Override
  public String[] freeVarNames() {
    List<String> names = new ArrayList<String>();
    for (IMonad<P> cref : _ruleMemory.monads())
      names.add(cref.getName());
    return names.toArray(new String[names.size()]);
  }

  @Override
  public void setFreeVariables(Map<String, IMonad> freeVars) {
    for (IMonad cref : freeVars.values())
      if (_ruleMemory.containsMonad(cref.getName()))
        _ruleMemory.setMonad(cref);
  }

  @Override
  public ActivationContext<P, ICARule<P>> getActivationContext() {
    return _ruleMemory;
  }
}
