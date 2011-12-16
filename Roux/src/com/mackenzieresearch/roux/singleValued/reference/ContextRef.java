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

package com.mackenzieresearch.roux.singleValued.reference;

import com.mackenzieresearch.roux.context.Monad;
import com.mackenzieresearch.roux.context.Monadex;
import com.mackenzieresearch.roux.kernel.*;

import java.util.Map;

public class ContextRef<T> implements ISideEffectFreeExpression<T, T> {
  private String _name = null;
  private IReadOnlyMonad<T> _monad = null;

  public ContextRef(String name) {
    _name = name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T evaluate(IMonadex context) {
    if (_monad == null)
      _monad = context.getMonad(_name);

    return _monad.bindValue(context);
  }

  @Override
  public IReadWriteMonadex freeVariables() {
    return new Monadex(new Monad<T>(_name));
  }

  @Override
  public String[] freeVarNames() {
    String[] freeVarNames = new String[1];
    freeVarNames[0] = _name;
    return freeVarNames;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setFreeVariables(Map<String, IMonad> freeVars) {
    IReadOnlyMonad<T> cref = freeVars.get(_name);
    if (cref != null)
      _monad =  cref;
  }

}
