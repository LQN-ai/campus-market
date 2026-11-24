package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.entity.Product;
import com.campus.market.vo.ProductVO;
import org.apache.ibatis.annotations.Param;

public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 条件搜索 + 排序（关联店铺名），见 ProductMapper.xml
     */
    IPage<ProductVO> search(Page<ProductVO> page,
                            @Param("keyword") String keyword,
                            @Param("category") String category,
                            @Param("sortBy") String sortBy);

    /**
     * 商品详情（关联店铺名）
     */
    ProductVO selectDetailWithShop(@Param("id") Long id);
}
