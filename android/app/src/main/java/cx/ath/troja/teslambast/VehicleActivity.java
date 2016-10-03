package cx.ath.troja.teslambast;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import go.golang.StateListener;

public class VehicleActivity extends Activity {

    public static final String VEHICLE_ID = "cx.ath.troja.teslambast.VehicleActivity.VEHICLE_ID";
    protected static Vehicle vehicle = null;
    private Handler handler = new Handler();

    protected void setConnectionState(boolean on) {
        ColorStateList colorStateList;
        if (on) {
            colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_enabled}
                    },
                    new int[]{Color.GREEN}
            );
        } else {
            colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_enabled}
                    },
                    new int[]{Color.RED}
            );
        }
        ((RadioButton) VehicleActivity.this.findViewById(R.id.connected)).setButtonTintList(colorStateList);
    }

    protected void disableControls() {
        ((Button) findViewById(R.id.homelink)).setEnabled(false);
        ((Button) findViewById(R.id.autoForward)).setEnabled(false);
        ((Button) findViewById(R.id.autoReverse)).setEnabled(false);
        setConnectionState(false);
    }

    @Override
    protected void onPause() {
        disableControls();
        vehicle.vehicle.close();
        super.onPause();
    }

    public void onResume() {
        super.onResume();

        disableControls();

        ((Button) findViewById(R.id.autoForward)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                vehicle.vehicle.autoparkForward();
                            } catch (final Exception e) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VehicleActivity.this, R.string.autoparking, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                vehicle.vehicle.autoparkAbort();
                            } catch (final Exception e) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VehicleActivity.this, R.string.autopark_aborted, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
                return true;
            }
        });

        ((Button) findViewById(R.id.autoReverse)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                vehicle.vehicle.autoparkReverse();
                            } catch (final Exception e) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VehicleActivity.this, R.string.autoparking, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                vehicle.vehicle.autoparkAbort();
                            } catch (final Exception e) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                                return;
                            }
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VehicleActivity.this, R.string.autopark_aborted, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
                return true;
            }
        });

        ((Button) findViewById(R.id.homelink)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            vehicle.vehicle.activateHomelink();
                        } catch (final Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VehicleActivity.this, R.string.homelink_activated, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });

        ((Button) findViewById(R.id.unlockCar)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            vehicle.vehicle.unlockCar();
                        } catch (final Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VehicleActivity.this, R.string.car_unlocked, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });

        ((Button) findViewById(R.id.unlockCharger)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            vehicle.vehicle.unlockCharger();
                        } catch (final Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(VehicleActivity.this, R.string.charging_port_unlocked, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    vehicle.vehicle.connect(new StateListener() {
                        @Override
                        public void autoparkReady(final boolean b) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((Button) VehicleActivity.this.findViewById(R.id.autoForward)).setEnabled(b);
                                    ((Button) VehicleActivity.this.findViewById(R.id.autoReverse)).setEnabled(b);
                                }
                            });
                        }

                        @Override
                        public void connectionUp(final boolean b) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    setConnectionState(b);
                                }
                            });
                        }

                        @Override
                        public void homelinkNearby(final boolean b) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((Button) VehicleActivity.this.findViewById(R.id.homelink)).setEnabled(b);
                                }
                            });
                        }
                    });
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        long vehicleID = getIntent().getLongExtra(VEHICLE_ID, 0);
        for (Vehicle v : ((Globals)getApplication()).vehicles) {
            if (v.id == vehicleID) {
                vehicle = v;
            }
        }

        if (vehicle == null) {
            Toast.makeText(this, R.string.invalid_vehicle, Toast.LENGTH_LONG).show();
            return;
        }

        setTitle(vehicle.name);
    }
}
