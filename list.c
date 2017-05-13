#include <ctype.h>
#include <dispatch/dispatch.h>
#include <errno.h>
#include <libproc.h>
#include <mach/mach.h>
#include <mach/task_info.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/sysctl.h>
#include <time.h>

mach_port_t task_for_pid_workaround()
{
  host_t        myhost = mach_host_self(); // host self is host priv if you're root anyway..
  mach_port_t   psDefault;
  mach_port_t   psDefault_control;

  task_array_t  tasks;
  mach_msg_type_number_t numTasks;
  int i;

   thread_array_t       threads;
   thread_info_data_t   tInfo;

  kern_return_t kr;

  kr = processor_set_default(myhost, &psDefault);

  kr = host_processor_set_priv(myhost, psDefault, &psDefault_control);
  if (kr != KERN_SUCCESS) { fprintf(stderr, "host_processor_set_priv failed with error %x\n", kr);
		 mach_error("host_processor_set_priv",kr); exit(1);}

  printf("So far so good\n");

  kr = processor_set_tasks(psDefault_control, &tasks, &numTasks);
  if (kr != KERN_SUCCESS) { fprintf(stderr,"processor_set_tasks failed with error %x\n",kr); exit(1); }

  for (i = 0; i < numTasks; i++)
        {
                printf("TASK %d\n", tasks[i]);
        }

   return (MACH_PORT_NULL);
} // end workaround



int main(int argc, char **argv) {
  task_for_pid_workaround();
}