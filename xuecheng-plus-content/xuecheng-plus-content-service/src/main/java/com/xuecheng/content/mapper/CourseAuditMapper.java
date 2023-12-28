package mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.entity.CourseAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * (CourseAudit)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-27 14:54:01
 */
@Mapper
public interface CourseAuditMapper extends BaseMapper<CourseAudit> {

}

