package it.unipi.gio.gioroom.rest.in;


import it.unipi.gio.gioroom.Logic;
import it.unipi.gio.gioroom.model.Goal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/goals")
public class GoalEndpoint {

    private Logic logic;

    private static final Logger LOG = LoggerFactory.getLogger(GoalEndpoint.class);
    private AtomicInteger id = new AtomicInteger(0);

    @Autowired
    public GoalEndpoint(Logic logic){
        this.logic=logic;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getGoals() {
        LOG.debug("Get goals request");
        List<Goal> goalList = logic.getGoals();
        return ResponseEntity.ok(goalList);
    }

    @RequestMapping(value="/{id}",method = RequestMethod.GET)
    public ResponseEntity getGoalById(@PathVariable("id") Integer id) {
        LOG.debug("Get goal by ID request, id : {}",id);
        Goal ret = logic.getGoalById(id);
        if(ret==null){
            LOG.debug("Get goal by ID {} not found",id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ret);
    }

    @RequestMapping(value="/user" , method = RequestMethod.GET)
    public ResponseEntity<List<Goal>> getGoalByUserId(@RequestParam(name="id", required = false) Integer id,
                                             @RequestParam(name="name", required = false) String name) {
        LOG.debug("Goal by user request, userID: {} or userName {}",id, name);
        if((id==null && name==null) || (id!=null && name!=null)){
            LOG.debug("Goal by user request refused, bad parameters");
            return ResponseEntity.badRequest().build();
        }
        List<Goal> list;
        if(id!=null){
            if(id<0){
                LOG.debug("Goal by user request refused, bad parameters");
                return ResponseEntity.badRequest().build();
            }
            list = logic.getGoalsByUserId(id);

        }else{
            if(name.equals("")){
                LOG.debug("Goal by user request refused, bad parameters");
                return ResponseEntity.badRequest().build();
            }
            list = logic.getGoalsByUser(name);
        }
        if(list.isEmpty()){
            LOG.debug("Goal by user request not found");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);

    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity setNewGoal(@RequestBody Goal g) {
        LOG.debug("Set new goal request");
        if(!g.checkValidity() || g.getBounds().containsKey(Goal.Property.TEMPERATURE) ||
            g.getBounds().containsKey(Goal.Property.SHUTTER_HEIGHT)){
            LOG.debug("Set new goal refused, bad request");
            return ResponseEntity.badRequest().build();
        }
        //TODO if(!logic.checkUser(g.getUser())){return ResponseEntity.status(HttpStatus.FORBIDDEN).build();};
        int id = this.id.getAndIncrement();
        g.setGoalId(id);
        Goal newG = logic.addGoal(g);
        return ResponseEntity.ok(newG);
    }

   /* @RequestMapping(value="/{id}" , method = RequestMethod.PUT)
    public ResponseEntity modifyGoal(@RequestBody Goal g, @PathVariable("id") int id){
        if(id >= this.id.get() || id < 0){return ResponseEntity.notFound().build();}
        g.setGoalId(id);
        id=findIndexById(id);
        goalList.set(id,g);
        return ResponseEntity.ok(g);
    }*/

    @RequestMapping(value="/{id}" , method = RequestMethod.DELETE)
    public ResponseEntity deleteGoal(@PathVariable("id") int id){
        LOG.debug("Delete goal request, id: {}",id);
        if(id<0){
            return ResponseEntity.badRequest().build();
        }
        if(!logic.deleteGoalById(id)){
            LOG.debug("Delete goal by id {} not found",id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value="/user" , method = RequestMethod.DELETE)
    public ResponseEntity deleteGoalByUser(@RequestParam(name="id", required = false) Integer id,
                                             @RequestParam(name="name", required = false) String name) {
        LOG.debug("Delete goal by user, id {} or name {}", id, name);
        if((id==null && name==null) || (id!=null && name!=null)){
            LOG.debug("Delete goal by user request refused, bad parameters");
            return ResponseEntity.badRequest().body("Either \"name\" or \"id\" parameters are required.");
        }
        if(id!=null){
            if(id<0){
                LOG.debug("Delete goal by user request refused, bad parameters");
                return ResponseEntity.badRequest().build();
            }
            if(!logic.deleteGoalByUser(id)){
                LOG.debug("Delete goal by user request refused, id {} not found", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        }else{
            if(name.equals("")){
                LOG.debug("Delete goal by user request refused, bad parameters");
                return ResponseEntity.badRequest().build();
            }
            if(!logic.deleteGoalByUser(name)){
                LOG.debug("Goal by user request refused, name {} not found",name);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        }

    }

    @RequestMapping(value="/ping",method = RequestMethod.GET)
    public ResponseEntity ping() {
        LOG.debug("Ping request");
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public void handleBadRequests(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), "JSON representing a goal is not well formed.");
    }

}
