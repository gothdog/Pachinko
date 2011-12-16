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

package com.mackenzieresearch.pachinko.test.rule;

import com.mackenzieresearch.pachinko.CARuleSystem;
import com.mackenzieresearch.pachinko.DefaultCARule;
import com.mackenzieresearch.pachinko.Variable;
import com.mackenzieresearch.roux.kernel.IMonad;
import com.mackenzieresearch.roux.kernel.IMonadex;
import com.mackenzieresearch.roux.kernel.IReadOnlyMonad;
import com.mackenzieresearch.roux.kernel.IReadWriteMonadex;
import org.junit.Test;

import static util.RuntimeAssertion.assertTrue;

public class ActivationTests {

  //
  //--------------------------------------------------------------------------------------------------------------------------
  //
  //  Rules used for tests...
  //

  public class VarVarVarRule extends DefaultCARule<String> {
    private int ruleActionFired = 0;

    public VarVarVarRule() {
      super();
      addVariable(new Variable("Var1", "UNINITIALIZED"));
      addVariable(new Variable("Var2", "UNINITIALIZED"));
      addVariable(new Variable("Var3", "UNINITIALIZED"));
    }

    @Override
    public boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context) {
      return true;
    }

    @Override
    public void doAction(IReadWriteMonadex<? extends IMonad> context) {
      ruleActionFired = ruleActionFired + 1;
    }

    public int getRuleActionFirings() {
      return ruleActionFired;
    }
  }

  public class PkVarVarVarRule extends DefaultCARule<String> {
    private int ruleActionFired = 0;

    public PkVarVarVarRule() {
      super();
      addPkVariable(new Variable("Var1", "UNINITIALIZED"));
      addVariable(new Variable("Var2", "UNINITIALIZED"));
      addVariable(new Variable("Var3", "UNINITIALIZED"));
    }

    @Override
    public boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context) {
      return true;
    }

    @Override
    public void doAction(IReadWriteMonadex<? extends IMonad> context) {
      ruleActionFired = ruleActionFired + 1;
    }

    public int getRuleActionFirings() {
      return ruleActionFired;
    }
  }

  public class PkVarPkVarVarRule extends DefaultCARule<String> {
    private int ruleActionFired = 0;

    public PkVarPkVarVarRule() {
      super();
      addPkVariable(new Variable("Var1", "UNINITIALIZED"));
      addPkVariable(new Variable("Var2", "UNINITIALIZED"));
      addVariable(new Variable("Var3", "UNINITIALIZED"));
    }

    @Override
    public boolean evaluateCondition(IMonadex<? extends IReadOnlyMonad> context) {
      return true;
    }

    @Override
    public void doAction(IReadWriteMonadex<? extends IMonad> context) {
      ruleActionFired = ruleActionFired + 1;
    }

    public int getRuleActionFirings() {
      return ruleActionFired;
    }
  }

  //
  //--------------------------------------------------------------------------------------------------------------------------
  //
  //  Tests...
  //

  @Test
  public void VarVarVarTest() {
    VarVarVarRule varVarVarRule = new VarVarVarRule();
    CARuleSystem<String> ruleSystem = new CARuleSystem<String>(varVarVarRule);
    IReadWriteMonadex context = ruleSystem.freeVariables();

    //  The VarVarVarRule should not activate until all three variables are bound.  Then it should
    //  activate when ANY of them change...
    context.returnValue("Var1", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(varVarVarRule.getRuleActionFirings() == 0);
    context.returnValue("Var2", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(varVarVarRule.getRuleActionFirings() == 0);
    context.returnValue("Var3", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(varVarVarRule.getRuleActionFirings() == 1);
    context.returnValue("Var1", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(varVarVarRule.getRuleActionFirings() == 2);
    context.returnValue("Var2", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(varVarVarRule.getRuleActionFirings() == 3);
    context.returnValue("Var3", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(varVarVarRule.getRuleActionFirings() == 4);
  }

  @Test
  public void PkVarVarVarRule() {
    PkVarVarVarRule pkVarVarVarRule = new PkVarVarVarRule();
    CARuleSystem<String> ruleSystem = new CARuleSystem<String>(pkVarVarVarRule);
    IReadWriteMonadex context = ruleSystem.freeVariables();

    //  The PkVarVarVarRule should not activate until all three variables are bound.  Then it should
    //  activate whenever the PKVariable changes but not when either of the other two change.
    context.returnValue("Var1", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarVarVarRule.getRuleActionFirings() == 0);
    context.returnValue("Var2", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarVarVarRule.getRuleActionFirings() == 0);
    context.returnValue("Var3", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarVarVarRule.getRuleActionFirings() == 1);
    context.returnValue("Var2", "ANOTHER_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarVarVarRule.getRuleActionFirings() == 1);
    context.returnValue("Var3", "ANOTHER_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarVarVarRule.getRuleActionFirings() == 1);
    context.returnValue("Var1", "ANOTHER_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarVarVarRule.getRuleActionFirings() == 2);
  }

  @Test
  public void PkVarPkVarVarRule() {
    PkVarPkVarVarRule pkVarPkVarVarRule = new PkVarPkVarVarRule();
    CARuleSystem<String> ruleSystem = new CARuleSystem<String>(pkVarPkVarVarRule);
    IReadWriteMonadex context = ruleSystem.freeVariables();

    //  The PkVarPkVarVar rule should not activate until all three variables are bound.  Thereafter,
    //  it should activate only when Var1 or Var2 (the PKVariables) changes...
    context.returnValue("Var1", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarPkVarVarRule.getRuleActionFirings() == 0);
    context.returnValue("Var2", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarPkVarVarRule.getRuleActionFirings() == 0);
    context.returnValue("Var3", "CHANGED_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarPkVarVarRule.getRuleActionFirings() == 1);
    context.returnValue("Var3", "ANOTHER_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarPkVarVarRule.getRuleActionFirings() == 1);
    context.returnValue("Var1", "ANOTHER_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarPkVarVarRule.getRuleActionFirings() == 2);
    context.returnValue("Var2", "ANOTHER_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarPkVarVarRule.getRuleActionFirings() == 3);
    context.returnValue("Var3", "ANOTHER_STATE");
    ruleSystem.executeActivations();
    assertTrue(pkVarPkVarVarRule.getRuleActionFirings() == 3);
  }
}
