package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckilltable;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by jianjun-wu on 2017/2/24.
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController
{
    private  final Logger logger= LoggerFactory.getLogger(this.getClass());


       @Autowired
       private SeckillService seckillService;

        @RequestMapping(value = "/list",method = RequestMethod.GET )
        public String list(Model model)
        {

           List<Seckilltable> list= seckillService.getSeckillList();
           model.addAttribute("list",list);
            return "list";
        }

        @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
        public  String detail(@PathVariable("seckillId") Long seckillId , Model model)
        {
            if(seckillId==null)
            {
                 return "redirect:/seckill/list";
            }

            Seckilltable seckilltable=seckillService.getById(seckillId);
            if(seckilltable==null)
            {
                 return "forward:/seckill/list";
            }

            model.addAttribute("seckill",seckilltable);

            return "detail";
        }

        //ajax json
        @RequestMapping(value ="/{seckillID}/exposer",
                method =RequestMethod.POST,
                produces = {"application/json;charset=UTF-8"})
        @ResponseBody
        public SeckillResult<Exposer> exposer(@PathVariable Long seckillID)
        {
            SeckillResult<Exposer> result;

            try
            {
                Exposer expose=  seckillService.exportSeckillUrl(seckillID);

                result=new SeckillResult<Exposer>(true,expose);

            }
            catch (Exception e)
            {
                logger.error(e.getMessage(),e);
                result=new SeckillResult<Exposer>(false,e.getMessage());
            }

            return result;
        }

        @RequestMapping(value = "/{seckillId}/{md5}/execution",
                method = RequestMethod.POST,
                produces = {"application/json;charset=UTF-8"})
        @ResponseBody
        public  SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                        @PathVariable("md5") String md5,
                                                        @CookieValue(value = "killPhone",required =false ) Long phone)
        {
            if(phone==null)
            {
                return new SeckillResult<SeckillExecution>(false,"未注册");
            }

            SeckillResult<SeckillException> result;
            try
            {
                SeckillExecution execution=seckillService.executeSeckillbyPro(seckillId,phone,md5);
                return new SeckillResult<SeckillExecution>(true,execution);
            }
            catch (RepeatKillException  e)
            {
                SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
                return new SeckillResult<SeckillExecution>(true,execution);

            }
            catch (SeckillCloseException e)
            {
                SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.END);
                return new SeckillResult<SeckillExecution>(true,execution);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(),e);
                SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
                return new SeckillResult<SeckillExecution>(true,execution);
            }
        }

        @RequestMapping(value = "/time/now",method = RequestMethod.GET)
        @ResponseBody
        public SeckillResult<Long>time()
        {
            Date now=new Date();
            return new SeckillResult(true,now.getTime());

        }





}
