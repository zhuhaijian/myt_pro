package com.zhc.myt.MytRestful.service.impl;


import java.util.Date;
import java.util.List;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zhc.myt.MytRestful.common.MytSystem;
import com.zhc.myt.MytRestful.service.MytUserService;
import com.zhc.myt.MytCommon.ReturnPage;
import com.zhc.myt.MytCommon.util.BeanUtils;
import com.zhc.myt.MytCommon.util.EhcacheUtil;
import com.zhc.myt.MytCommon.util.ExampleUtils;
import com.zhc.myt.MytCommon.util.MD5Util;
import com.zhc.myt.MytCommon.util.UUIDUtil;
import com.zhc.myt.MytDao.entity.MytMerchant;
import com.zhc.myt.MytDao.entity.MytUser;
import com.zhc.myt.MytDao.entity.MytUserExample;
import com.zhc.myt.MytDao.mapper.MytMerchantMapper;
import com.zhc.myt.MytDao.mapper.MytUserMapper;

@Service
public class MytUserServiceImpl implements MytUserService {

	@Autowired
	private MytUserMapper mytUserMapper;
	@Autowired
	private MytMerchantMapper mytMerchantMapper;

	public MytUserServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void add(MytUser t) {
		// TODO Auto-generated method stub
		Integer currentUserId = (Integer) MytSystem.getCurrentUserId();
		Date d = new Date();
		t.setCreateDate(d);
		t.setOptDate(d);
		t.setCreateId(currentUserId);
		t.setOptId(currentUserId);
		t.setStatus("1");
		if (t.getUserPassword() != null && !t.getUserPassword().equals("")) {
			t.setUserPassword(MD5Util.getMD5Lower(t.getUserPassword()));
		}
		mytUserMapper.insertSelective(t);
	}

	@Override
	public void delete(MytUser t) {
		// TODO Auto-generated method stub
		Integer currentUserId = (Integer) MytSystem.getCurrentUserId();
		Date d = new Date();
		t.setOptDate(d);
		t.setOptId(currentUserId);
		t.setStatus("0");
		mytUserMapper.updateByPrimaryKeySelective(t);
	}

	@Override
	public void update(MytUser t) {
		// TODO Auto-generated method stub
		Integer currentUserId = (Integer) MytSystem.getCurrentUserId();
		Date d = new Date();
		t.setOptDate(d);
		t.setOptId(currentUserId);
		if (t.getUserPassword() != null && !t.getUserClass().equals("")) {
			t.setUserPassword(MD5Util.getMD5Lower(t.getUserPassword()));
		}
		mytUserMapper.updateByPrimaryKeySelective(t);
	}

	@Override
	public MytUser getById(int id) {
		// TODO Auto-generated method stub
		return mytUserMapper.selectByPrimaryKey(id);
	}

	@Override
	public ReturnPage<MytUser> getByPage(Integer pageNumber, Integer pageSize,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		MytUserExample example = new MytUserExample();
		ExampleUtils.Map2ExampleMethod(example.or(), params);
		Integer limtStart=(pageNumber - 1) * pageSize;
		Integer limtEnd=limtStart+pageSize;
		example.setLimitStart(limtStart);
		example.setLimitEnd(limtEnd);
		example.setOrderByClause("create_date DESC");
		List<MytUser> content = mytUserMapper.selectByExample(example);
		Integer count = mytUserMapper.countByExample(example);
		ReturnPage<MytUser> re = new ReturnPage<MytUser>(count,
				pageNumber, pageSize, content);
		return re;
	}

	@Override
	public List<MytUser> getByList(Map<String, Object> params) {
		// TODO Auto-generated method stub
		MytUserExample example = new MytUserExample();
		ExampleUtils.Map2ExampleMethod(example.or(), params);
		example.setOrderByClause("create_date DESC");
		List<MytUser> content = mytUserMapper.selectByExample(example);
		return content;
	}

	@Override
	public Map<String, Object> login(String userName, String userPassword) {
		// TODO Auto-generated method stub
		if (userName == null || userName.equals("") || userPassword == null
				|| userPassword.equals("")) {
			return null;
		}

		MytUserExample example = new MytUserExample();
		example.or().andUserNameEqualTo(userName)
				.andUserPasswordEqualTo(MD5Util.getMD5Lower(userPassword));
		List<MytUser> list = mytUserMapper.selectByExample(example);
		if (list.size() > 0) {
			Cache service = (Cache) EhcacheUtil.getCache("tokenCache");
			String token = UUIDUtil.getUUID();
			MytUser user = list.get(0);
			// 判断 是否商户删除状态
			if (user.getUserClass()!=null && user.getUserClass().equals("1")) {
				MytMerchant mytMerchant = mytMerchantMapper
						.selectByPrimaryKey(user.getPartentId());
				if (mytMerchant == null || mytMerchant.getStatus().equals("0")) {
					return null;
				}
			}
			Map<String, Object> rmap = BeanUtils.Bean2Map(user);
			rmap.put("token", token);
			rmap.remove("userPassword");
			service.put(new Element(MytSystem.SYS_CACHE_PREFIX + token, user
					.getId()));
			return rmap;
		}
		return null;
	}

}
