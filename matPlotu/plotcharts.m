clear

N1data=load("../N1/N1out.mat");
N2data=load("../N2/N2out.mat");
%Wdata=load("../Workload/roi_profile.mat");

n1d=N1data.rt(1:10:end);
n2d=N2data.rt(1:10:end);

figure
hold on
title("N1")
stairs(n1d);
plot(cumsum(n1d)./linspace(1,size(n1d,2),size(n1d,2)));
yline(0.6,'-.','Threshold');


figure
hold on
title("N2")
stairs(N2data.rt);
plot(cumsum(N2data.rt)./linspace(1,size(N2data.rt,2),size(N2data.rt,2)));
yline(0.25,'-.','Threshold');