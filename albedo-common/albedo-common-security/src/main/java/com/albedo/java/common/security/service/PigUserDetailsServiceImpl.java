/*
 *  Copyright (c) 2019-2020, 冷冷 (somewhere0813@gmail.com).
 *  <p>
 *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 * https://www.gnu.org/licenses/lgpl.html
 *  <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.albedo.java.common.security.service;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.albedo.java.modules.sys.vo.UserInfo;
import com.albedo.java.modules.sys.domain.User;
import com.albedo.java.modules.sys.feign.RemoteUserService;
import com.albedo.java.common.core.constant.CommonConstants;
import com.albedo.java.common.core.constant.SecurityConstants;
import com.albedo.java.common.core.util.R;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户详细信息
 *
 * @author somewhere
 */
@Slf4j
@Service
@AllArgsConstructor
public class PigUserDetailsServiceImpl implements UserDetailsService {
	private final RemoteUserService remoteUserService;
	private final CacheManager cacheManager;



	/**
	 * 用户密码登录
	 *
	 * @param username 用户名
	 * @return
	 */
	@Override
	@SneakyThrows
	public UserDetails loadUserByUsername(String username) {
		Cache cache = cacheManager.getCache("user_details");
//		if (cache != null && cache.get(username) != null) {
//			return (PigUser) cache.get(username).get();
//		}

		R<UserInfo> result = remoteUserService.info(username, SecurityConstants.FROM_IN);
		UserDetails userDetails = getUserDetails(result);
		cache.put(username, userDetails);
		return userDetails;
	}

	/**
	 * 构建userdetails
	 *
	 * @param result 用户信息
	 * @return
	 */
	private UserDetails getUserDetails(R<UserInfo> result) {
		if (result == null || result.getData() == null) {
			throw new UsernameNotFoundException("用户不存在");
		}

		UserInfo info = result.getData();
		Set<String> dbAuthsSet = new HashSet<>();
		if (ArrayUtil.isNotEmpty(info.getRoles())) {
			// 获取角色
			Arrays.stream(info.getRoles()).forEach(role -> dbAuthsSet.add(SecurityConstants.ROLE + role));
			// 获取资源
			dbAuthsSet.addAll(Arrays.asList(info.getPermissions()));

		}
		Collection<? extends GrantedAuthority> authorities
			= AuthorityUtils.createAuthorityList(dbAuthsSet.toArray(new String[0]));
		User user = info.getUser();

		// 构造security用户
		return new PigUser(user.getId(), user.getDeptId(), user.getUsername(), SecurityConstants.BCRYPT + user.getPassword(),
			StrUtil.equals(user.getLockFlag(), CommonConstants.STATUS_NORMAL), true, true, true, authorities);
	}
}
