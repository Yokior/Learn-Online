package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess>
{
    /**
     * 根据分片索引查询需要分配的任务 最大重试次数：3
     * @param shardTotal 分片总数
     * @param shardIndex 分片索引（要分配给谁）
     * @param count 查询个数
     * @return
     */
    @Select("select * from media_process m where m.id % #{shardTotal} = #{shardIndex} and (m.status = '1' or m.status = '3') and m.fail_count < 3 limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);


    /**
     * 开启一个任务
     * @param id
     * @return
     */
    @Update("update media_process m set m.status = '4' where (m.status = '1' or m.status = '3') and m.fail_count < 3 and m.id = #{id}")
    int startTask(@Param("id") long id);

}
