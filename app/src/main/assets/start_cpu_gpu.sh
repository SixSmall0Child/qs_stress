mount -t debugfs debugfs /sys/kernel/debug
stop thermald
stop thermal-engine
stop thermal-engine-v2
echo 4 > /sys/devices/system/cpu/cpu0/core_ctl/min_cpus
echo 4 > /sys/devices/system/cpu/cpu4/core_ctl/min_cpus
echo 1 > /sys/devices/system/cpu/cpu1/online
echo 1 > /sys/devices/system/cpu/cpu2/online
echo 1 > /sys/devices/system/cpu/cpu3/online
echo 1 > /sys/devices/system/cpu/cpu4/online
echo 1 > /sys/devices/system/cpu/cpu5/online
echo 1 > /sys/devices/system/cpu/cpu6/online
echo 1 > /sys/devices/system/cpu/cpu7/online
echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor
echo performance > /sys/devices/system/cpu/cpu1/cpufreq/scaling_governor
echo performance > /sys/devices/system/cpu/cpu2/cpufreq/scaling_governor
echo performance > /sys/devices/system/cpu/cpu3/cpufreq/scaling_governor
echo performance > /sys/devices/system/cpu/cpu4/cpufreq/scaling_governor
echo performance > /sys/devices/system/cpu/cpu5/cpufreq/scaling_governor
echo performance > /sys/devices/system/cpu/cpu6/cpufreq/scaling_governor
echo performance > /sys/devices/system/cpu/cpu7/cpufreq/scaling_governor
echo 1 > /sys/module/lpm_levels/parameters/sleep_disabled
echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on
echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on
echo 1 >/sys/class/kgsl/kgsl-3d0/force_clk_on
echo 10000000 >/sys/class/kgsl/kgsl-3d0/idle_timer
echo performance >/sys/class/kgsl/kgsl-3d0/devfreq/governor
echo 0 >/sys/class/kgsl/kgsl-3d0/gpuclk
echo 0 > /sys/class/kgsl/kgsl-3d0/devfreq/max_freq
echo 0 > /sys/class/kgsl/kgsl-3d0/devfreq/min_freq
echo 0 > /sys/class/kgsl/kgsl-3d0/hwcg
echo 0 > /sys/class/kgsl/kgsl-3d0/ifpc



