/*

   ROUX, A foundation library for simple monadic expressions.
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

package com.mackenzieresearch.roux.existential;

import com.mackenzieresearch.roux.context.NullMonadex;
import com.mackenzieresearch.roux.context.Monadex;
import com.mackenzieresearch.roux.kernel.*;

import java.util.*;

public class Equals<Q, L extends ISideEffectFreeExpression<?, Q>, R extends ISideEffectFreeExpression<?, Q>> implements IExistentialExpression<Q> {
  private ISideEffectFreeExpression<?, Q> _lParam = null;
  private ISideEffectFreeExpression<?, Q> _rParam = null;

  public Equals() {
  }

  public Equals(L lParam, R rParam) {
    _lParam = lParam;
    _rParam = rParam;
  }

  public Equals(L lParam, Q rParam) {
    _lParam = lParam;
    _rParam = new IdentityExpression<Q>(rParam);
  }

  public Equals(Q lParam, R rParam) {
    _lParam = new IdentityExpression<Q>(lParam);
    _rParam = rParam;
  }

  public Equals(Q lParam, Q rParam) {
    _lParam = new IdentityExpression<Q>(lParam);
    _rParam = new IdentityExpression<Q>(rParam);
  }

  public Equals<Q, L, R> leftParam(L lParam) {
    _lParam = lParam;
    return this;
  }

  public Equals<Q, L, R> leftParam(Q lParam) {
    _lParam = new IdentityExpression<Q>(lParam);
    return this;
  }

  public Equals<Q, L, R> rightParam(R rParam) {
    _rParam = rParam;
    return this;
  }

  public Equals<Q, L, R> rightParam(Q rParam) {
    _rParam = new IdentityExpression<Q>(rParam);
    return this;
  }


  @Override
  public Boolean evaluate(IMonadex iReadOnlyMonadIContext) {
    Q lParam = _lParam.evaluate(iReadOnlyMonadIContext);
    Q rParam = _rParam.evaluate(iReadOnlyMonadIContext);

    if (lParam == null)
      if (rParam == null)
        return Boolean.TRUE;
      else
        return Boolean.FALSE;
    else
      return lParam.equals(rParam);
  }

  @Override
  public IReadWriteMonadex freeVariables() {
    IReadWriteMonadex<IMonad<?>> lVars = _lParam != null ? _lParam.freeVariables() : new NullMonadex();
    IReadWriteMonadex<IMonad<?>> rVars = _rParam != null ? _rParam.freeVariables() : new NullMonadex();


    ArrayList<IMonad<?>> freeVariables = new ArrayList<IMonad<?>>();
    Map<String, IMonad> freeVarIndex = new HashMap<String, IMonad>();

    for (IMonad<?> cref : lVars.monads())
      if (!freeVarIndex.containsKey(cref.getName())) {
        freeVariables.add(cref);
        freeVarIndex.put(cref.getName(), cref);
      }

    for (IMonad<?> cref : rVars.monads())
      if (!freeVarIndex.containsKey(cref.getName())) {
        freeVariables.add(cref);
        freeVarIndex.put(cref.getName(), cref);
      }

    setFreeVariables(freeVarIndex);

    return new Monadex(freeVariables.toArray(new IMonad<?>[freeVariables.size()]));
  }

  @Override
  public String[] freeVarNames() {
    String[] lFreeVarNames = _lParam != null ? _lParam.freeVarNames() : new String[0];
    String[] rFreeVarNames = _rParam != null ? _rParam.freeVarNames() : new String[0];

    List<String> freeVars = Arrays.asList(lFreeVarNames);
    freeVars.addAll(Arrays.asList(rFreeVarNames));
    return freeVars.toArray(new String[0]);
  }

  @Override
  public void setFreeVariables(Map<String, IMonad> freeVars) {
    _lParam.setFreeVariables(freeVars);
    _rParam.setFreeVariables(freeVars);
  }

  protected static class IdentityExpression<Q> implements ISideEffectFreeExpression<Q, Q> {
    private Q _param = null;

    public IdentityExpression(Q param) {
      _param = param;
    }

    @Override
    public Q evaluate(IMonadex iReadOnlyMonadIContext) {
      return _param;
    }

    @Override
    public IReadWriteMonadex freeVariables() {
//      return new Monad(new Monad<Q>("value"));
      return new Monadex();
    }

    @Override
    public String[] freeVarNames() {
      String[] freeVarNames = new String[1];
      freeVarNames[0] = "value";
      return freeVarNames;
    }

    @Override
    public void setFreeVariables(Map<String, IMonad> freeVars) {
    }
  }
}
