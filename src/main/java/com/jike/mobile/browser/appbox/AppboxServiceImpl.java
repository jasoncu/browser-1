package com.jike.mobile.browser.appbox;

import java.util.List;

import com.jike.mobile.browser.common.crawler.CrawlerMatcher;
import com.jike.mobile.browser.common.crawler.crawlerException;
import com.jike.mobile.browser.dao.AppboxCategoryDao;
import com.jike.mobile.browser.dao.AppboxItemDao;
import com.jike.mobile.browser.model.AppboxCategory;
import com.jike.mobile.browser.model.AppboxItem;
import com.jike.mobile.browser.util.ServiceException;

public class AppboxServiceImpl implements AppboxService{
	
	// inject
	AppboxCategoryDao appboxCategoryDao;
	AppboxItemDao appboxItemDao;

	public AppboxItemDao getAppboxItemDao() {
		return appboxItemDao;
	}

	public void setAppboxItemDao(AppboxItemDao appboxItemDao) {
		this.appboxItemDao = appboxItemDao;
	}

	public AppboxCategoryDao getAppboxCategoryDao() {
		return appboxCategoryDao;
	}

	public void setAppboxCategoryDao(AppboxCategoryDao appboxCategoryDao) {
		this.appboxCategoryDao = appboxCategoryDao;
	}

	@Override
	public Integer addCategory(AppboxCategory appboxCategory) {
		appboxCategory.setPostTime(System.currentTimeMillis());
		return (Integer)appboxCategoryDao.save(appboxCategory);	
	}

	@Override
	public AppboxCategory findCategoryById(int appboxCategoryId) {
		return appboxCategoryDao.findById(appboxCategoryId);
	}

	@Override
	public void updateCategory(AppboxCategory appboxCategory) {
		appboxCategoryDao.update(appboxCategory);	
	}

	@Override
	public List<AppboxCategory> listCategoryByPageDesc(int page, int page_size) {
		return appboxCategoryDao.findByPageOrderByProperty(page, page_size, "id", true);
	}

	@Override
	public void deleteCategory(AppboxCategory appboxCategory) {
		List<AppboxItem> list = appboxItemDao.findByProperty("appboxCategory", appboxCategory);
		if(list.size() > 0) {
			throw new ServiceException("appbox.category.is.not.empty");
		}
		appboxCategoryDao.delete(appboxCategory);
	}

	@Override
	public List<AppboxCategory> findCategoryAll() {
		return appboxCategoryDao.findAll();
	}

	@Override
	public Integer addItem(AppboxItem appboxItem) {
		AppboxCategory appboxCategory = findCategoryById(appboxItem.getAppboxCategory().getId());
		if(appboxCategory == null) throw new ServiceException("appbox.category.is.not.exist");
		
		appboxItem.setPostTime(System.currentTimeMillis());
		return (Integer)appboxItemDao.save(appboxItem);
	}

	@Override
	public void updateItem(AppboxItem appboxItem) {
		AppboxCategory appboxCategory = findCategoryById(appboxItem.getAppboxCategory().getId());
		if(appboxCategory == null) throw new ServiceException("appbox.category.is.not.exist");
		appboxItemDao.update(appboxItem);
	}

	@Override
	public AppboxItem findItemById(int appboxItemId) {
		return appboxItemDao.findById(appboxItemId);
	}

	@Override
	public void deleteItem(int appboxItemId) {
		AppboxItem appboxItem = appboxItemDao.findById(appboxItemId);
		if(appboxItem == null) throw new ServiceException("appbox.item.is.not.exist");
		appboxItemDao.delete(appboxItem);
	}

	@Override
	public List<AppboxItem> findItemByPageDesc(int page, int page_size) {
		return appboxItemDao.findByPageOrderByProperty(page, page_size, "id", true);
	}
	
	@Override
	public List<AppboxItem> findItemAll() {
		return appboxItemDao.findAll();
	}
	
	@Override
	public int match(int appboxItemId) {
		AppboxItem appboxItem = appboxItemDao.findById(appboxItemId);
		if(appboxItem == null) throw new ServiceException("appbox.item.is.not.exist");
		return match(appboxItem);
	}
	
	public int match(AppboxItem appboxItem) {
		
		CrawlerMatcher cm = new CrawlerMatcher();
		try {
			cm.setUrl(appboxItem.getSource());
			String[] regexs = {appboxItem.getTitleRegex(), appboxItem.getUrlRegex(), appboxItem.getPicRegex()};
			
			cm.setRegexs(regexs);
			cm.setCharSet(appboxItem.getCharSet());
			cm.execute();
			
			int isUpdate = 0;
			if(cm.getResult()[0] == null || !cm.getResult()[0].equals(appboxItem.getTitle())){
				appboxItem.setTitle(cm.getResult()[0]);
				isUpdate = 1;
			}
			if(cm.getResult()[1] == null || !cm.getResult()[1].equals(appboxItem.getUrl())){
				appboxItem.setUrl(cm.getResult()[1]);
				isUpdate = 1;
			}
			if(cm.getResult()[2] == null || !cm.getResult()[2].equals(appboxItem.getImgUrl())){
				appboxItem.setImgUrl(cm.getResult()[2]);
				isUpdate = 1;
			}
			
			if(isUpdate == 1) {
				appboxItem.setMatchTime(System.currentTimeMillis());
			}
			
			int statue;
			
			if(cm.getResult()[0] == null && cm.getResult()[1] == null && cm.getResult()[2] == null) statue = -1;
			else if(cm.getResult()[0] != null && cm.getResult()[1] != null && cm.getResult()[2] != null) statue = 0;
			else statue = 1;
			
			appboxItem.setMatchStatue(statue);
			
			appboxItemDao.update(appboxItem);
			return statue;
			
		} catch (crawlerException e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage(), e);
		}
	}

	
}
















