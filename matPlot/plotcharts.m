clear

N1data=load("../N1/N1out.mat");
N2data=load("../N2/N2out.mat");
Wdata=load("../Workload/roi_profile.mat");

Wdata.roi=[300,Wdata.roi(1,1:end-1)];
slai=[0.25,0.35,0.55,0.20,0.60,0.25];
rates=[];
slas=[];
for i=1:size(Wdata.roi,2)
    if(i==1)
        rates=[rates,repmat(Wdata.roi(i),1,sum(N1data.ctime<Wdata.ctime(1,i)))];
        slas=[slas,repmat(slai(i),1,sum(N1data.ctime<Wdata.ctime(1,i)))];
    else
        rates=[rates,repmat(Wdata.roi(i),1,sum(N1data.ctime>Wdata.ctime(1,i-1) ...
                          & N1data.ctime<Wdata.ctime(1,i)) )];
        slas=[slas,repmat(slai(i),1,sum(N1data.ctime>Wdata.ctime(1,i-1) ...
                          & N1data.ctime<Wdata.ctime(1,i)) )];
    end
end

slas=slas(1:end-1);

startTime=0;

n1d=N1data.rt(1,1:end-1);
n2d=N2data.rt;

N1req=0.25;
N2req=0.15;

n1Cum=cumsum(n1d)./linspace(1,size(n1d,2),size(n1d,2));
n2Cum=cumsum(n2d)./linspace(1,size(n2d,2),size(n2d,2));

% figure
% hold on
% title("N1_rt")
% plot(n1Cum);
% stairs(smoothdata(n1d,"movmean"));
% yline(N1req,'-.');
% grid on;
% box on;
% e1=abs(n1Cum(1,end)-N1req)*100/N1req;

% figure
% hold on
% title("N2")
% stairs(smoothdata(n2d,"movmean"));
% plot(n2Cum);
% yline(N2req,'-.');
% grid on;
% box on;
% e2=abs(n2Cum(1,end)-N2req)*100/N2req;
% 
% figure
% hold on
% title("Core")
% stairs(N1data.core);
% stairs(N2data.core);
% grid on;
% box on;
% legend("N1","N2")
% 
% figure
% hold on
% title("u")
% stairs(N1data.u);
% stairs(N2data.u);
% grid on;
% box on;
% legend("N1","N2")
% 
% figure
% hold on
% title("roi")
% stairs(rates);
% grid on;
% box on;

x=(N2data.ctime-N2data.ctime(1))/(10^9);

figure('units','normalized','outerposition',[0 0 1 1])
subplot(7,1,1);
stairs((N1data.ctime-N1data.ctime(1))/(10^9),rates);
ylim([min(rates),max(rates)*1.05])
title('(a) input rate [req/s]') 
grid on;
box on;

save( 'a_1.txt','x','rates', '-ASCII' );

subplot(7,1,2);
hold on
stairs(x,n2d+n1d);
% plot(x,n1Cum+n2Cum,"LineWidth",1.5)
plot(x,slas+N2req,'-.',"LineWidth",1.5);
title('(b) overall response time [s]') 
legend("istantaneous","average","setpoint","Orientation","horizontal")
grid on;
box on;

s_t=n2d+n1d;
s_avg=n1Cum+n2Cum;
reqT=N2req+N1req;

save( 'b_1.txt','x','s_t','s_avg','reqT','-ASCII' );

subplot(7,1,3);
hold on
stairs(x,smoothdata(n2d+n1d,"movmean",4));
% plot(x,n1Cum+n2Cum,"LineWidth",1.5)
plot(x,N2req+slas,'-.',"LineWidth",1.5);
title('(c) overall response time, 5 seconds average [s]') 
legend("istantaneous","average","setpoint","Orientation","horizontal")
grid on;
box on;


s_smooth=smoothdata(n2d+n1d,"movmean",4);
s_avg=n1Cum+n2Cum;
reqT=N2req+slas;
save( 'c_1.txt','x','s_smooth','s_avg','reqT','-ASCII' );

subplot(7,1,4);
hold on
stairs(x,n1d);
% plot(x,n1Cum,"LineWidth",1.5)
plot(x,slas,'-.',"LineWidth",1.5);
stairs(x,n2d);
% plot(x,n2Cum,"LineWidth",1.5)
yline(N2req,'-.',"LineWidth",1.5);
title('(d) tier response time [s]') 
legend("N1 istantaneous","N1 average","N1 setpoint","N2 istantaneous","N2 average","N2 setpoint","Orientation","horizontal")
grid on;
box on;

save( 'd_1.txt','x','n1d','n1Cum','N1req','n2d','n2Cum','N2req','-ASCII' );

subplot(7,1,5);
hold on
plot(x,N1data.u(1,1:end-1),"LineWidth",1.5);
plot(x,N2data.u,"LineWidth",1.5,"LineStyle",'-.')
title('(e)  linearised control signals') 
legend("N1","N2","Orientation","horizontal","Location","southeast")
grid on;
box on;

u1=N1data.u(1:end-1);
u2=N2data.u;

save( 'e_1.txt','x','u1','u2','-ASCII' );

subplot(7,1,6);
hold on
plot(x,N1data.core(1,1:end-1),"LineWidth",1.5);
plot(x,N2data.core,"LineWidth",1.5,"LineStyle",'-.')
title('(f)  allotted resources [cores]') 
legend("N1","N2","Orientation","horizontal")
grid on;
box on;

c1=N1data.core(1:end-1);
c2=N2data.core;

save( 'f_1.txt','x','c1','c2','-ASCII' );

subplot(7,1,7);
hold on
plot(x,[0,diff(N1data.ctime(1,1:end-1)/10^9)],"LineWidth",1.5);
plot(x,[0,diff(N2data.ctime/10^9)],"LineWidth",1.5,"LineStyle",'-.')
title('(g)  time between control actions [s]') 
legend("N1","N2","Orientation","horizontal")
grid on;
box on;
xlabel("time (s)")

t1=[0,diff(N1data.ctime(1:end-1)/10^9)];
t2=[0,diff(N2data.ctime/10^9)];

save( 'f_1.txt','x','t1','t2','-ASCII' );
   
%time,roi,tauro,taur,tauro1,taur1,u1,cores1,Ts1,tauro2,taur2,u2,cores2,Ts2,Avg1,
M=[ x',rates(1:end-1)',ones(size(rates(1:end-1),2),1).*(slas+N2req)',(n2d+n1d)',...
    slas',n1d',u1',c1',t1',...
    ones(size(rates(1:end-1),2),1).*(N2req),n2d',u2',c2',t2',...
    smoothdata(n2d+n1d,"movmean",4)'];

% writematrix(M,'expData.csv') 




%tempo,roi,tauro,taur,tauro1,taur1,tauro2,taur2,u1,core1,u2,core2,

% SLA totale (tauro) cambita 1-2 volte nella prova
% 	 
% 	
% SLA per i due tier:
% 	
% 	tauro2 = tauro*sigma_stiamto_2/(sigma_stimato_1+sigma_stimato_2)
% 	
% 	tauro1 = tauro-taur2
% 	(la misura di taur del secondo tier, NON il set point)

% set(gcf,'color','w');
% exportgraphics(gcf,'realsim1.pdf')
% close()
