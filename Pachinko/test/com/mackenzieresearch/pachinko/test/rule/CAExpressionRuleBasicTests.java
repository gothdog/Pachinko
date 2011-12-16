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

import com.mackenzieresearch.pachinko.CAExpressionRule;
import com.mackenzieresearch.pachinko.CARuleSystem;
import com.mackenzieresearch.roux.context.Monadex;
import com.mackenzieresearch.roux.existential.Equals;
import com.mackenzieresearch.roux.kernel.IMonad;
import com.mackenzieresearch.roux.kernel.IReadOnlyMonad;
import com.mackenzieresearch.roux.kernel.IReadWriteMonadex;
import com.mackenzieresearch.roux.kernel.ISideEffectFreeAction;
import com.mackenzieresearch.roux.singleValued.reference.ContextRef;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static util.RuntimeAssertion.assertTrue;

public class CAExpressionRuleBasicTests {
  private class RuleEvaluatedTrueException extends RuntimeException {
    private String _ruleName;

    public RuleEvaluatedTrueException(String ruleName) {
      _ruleName = ruleName;
    }

    public String getRuleName() {
      return _ruleName;
    }
  }

  private class RuleEvaluatedFalseException extends RuntimeException {
    private String _ruleName;

    public RuleEvaluatedFalseException(String ruleName) {
      _ruleName = ruleName;
    }

    public String getRuleName() {
      return _ruleName;
    }
  }

  private class ThrowExceptionAction implements ISideEffectFreeAction<Object> {
    private RuntimeException _exception = null;

    public ThrowExceptionAction(RuntimeException exception) {
      _exception = exception;
    }

    @Override
    public void evaluate(IReadWriteMonadex<? extends IReadOnlyMonad<Object>> context) {
      throw _exception;
    }

    @Override
    public IReadWriteMonadex freeVariables() {
      return new Monadex(new IMonad[0]);
    }

    @Override
    public String[] freeVarNames() {
      return new String[0];
    }

    @Override
    public void setFreeVariables(Map<String, IMonad> freeVars) {
    }
  }

  private class NoOpAction implements ISideEffectFreeAction<Object> {

    @Override
    public void evaluate(IReadWriteMonadex<? extends IReadOnlyMonad<Object>> context) {
    }

    @Override
    public IReadWriteMonadex freeVariables() {
      return new Monadex(new IMonad[0]);
    }

    @Override
    public String[] freeVarNames() {
      return new String[0];
    }

    @Override
    public void setFreeVariables(Map<String, IMonad> freeVars) {
    }
  }


  @Test
  public void helloTrueRule() {
    CAExpressionRule<Integer> rule = new CAExpressionRule()
            .whereCondition(new Equals()
                    .leftParam(new ContextRef<Integer>("sampleData"))
                    .rightParam(10))
            .performAction(new ThrowExceptionAction(new RuleEvaluatedTrueException("EqualsTen")));

    CARuleSystem<Integer> ruleSystem = new CARuleSystem<Integer>(rule);

    try {
      IReadWriteMonadex context = ruleSystem.freeVariables();
      context.returnValue("sampleData", 10);
      ruleSystem.executeActivations();
      assertFalse("Rule did not fire.", true);
    } catch (RuleEvaluatedTrueException rf) {
      if (rf.getRuleName().equals("EqualsTen"))
        assertTrue(true);
      else
        assertFalse("Rule did not fire.  Some other error happened.", true);
    }
  }

  @Test
  public void helloFalseRule() {
    CAExpressionRule<Integer> rule = new CAExpressionRule()
            .whereCondition(new Equals()
                    .leftParam(new ContextRef<Integer>("sampleData"))
                    .rightParam(11))
            .performAction(new ThrowExceptionAction(new RuleEvaluatedFalseException("EqualsTen")));

    CARuleSystem<Integer> ruleSystem = new CARuleSystem<Integer>(rule);

    try {
      IReadWriteMonadex context = ruleSystem.freeVariables();
      context.returnValue("sampleData", 10);
      ruleSystem.executeActivations();
      assertTrue(true);
    } catch (RuleEvaluatedTrueException rf) {
      if (rf.getRuleName().equals("EqualsTen"))
        assertFalse("Rule should not have fired.", true);
      else
        assertFalse("Some other error happened evaluating rule.", true);
    }
  }
}
