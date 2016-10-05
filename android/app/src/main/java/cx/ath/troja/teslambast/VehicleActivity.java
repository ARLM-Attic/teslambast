package cx.ath.troja.teslambast;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import go.golang.ClimateState;
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

        ((Button) findViewById(R.id.ac)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressBar = new ProgressDialog(VehicleActivity.this);
                progressBar.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ClimateState climateState;
                        try {
                            climateState = vehicle.vehicle.climateState();
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(VehicleActivity.this);
                                builder.setTitle(R.string.ac);

                                LayoutInflater inflater = VehicleActivity.this.getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.climate_input, null);

                                final CheckBox on = ((CheckBox) dialogView.findViewById(R.id.on));
                                final Spinner temp = (Spinner) dialogView.findViewById(R.id.temp);

                                on.setChecked(climateState.getOn());
                                on.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if (on.isChecked()) {
                                                        vehicle.vehicle.startAirConditioning();
                                                    } else {
                                                        vehicle.vehicle.stopAirConditioning();
                                                    }
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
                                                        if (on.isChecked()) {
                                                            Toast.makeText(VehicleActivity.this, R.string.ac_on, Toast.LENGTH_SHORT).show();
                                                            temp.setEnabled(true);
                                                        } else {
                                                            Toast.makeText(VehicleActivity.this, R.string.ac_off, Toast.LENGTH_SHORT).show();
                                                            temp.setEnabled(false);
                                                        }
                                                    }
                                                });
                                            }
                                        }).start();
                                    }
                                });

                                temp.setEnabled(climateState.getOn());
                                final ArrayAdapter<String> tempAdapter = new ArrayAdapter<String>(VehicleActivity.this, android.R.layout.simple_spinner_item, new ArrayList<String>());
                                int minTemp = 16;
                                int maxTemp = 37;
                                for (int i = minTemp; i < maxTemp; i++) {
                                    tempAdapter.add("" + i);
                                }
                                temp.setAdapter(tempAdapter);
                                int currTemp = (int) climateState.getTemp();
                                final ArrayList<Integer> currTempContainer = new ArrayList<Integer>();
                                currTempContainer.add(currTemp);
                                if (currTemp >= minTemp && currTemp <= maxTemp) {
                                    temp.setSelection(currTemp - minTemp);
                                } else if (currTemp < minTemp) {
                                    temp.setSelection(0);
                                } else {
                                    temp.setSelection(tempAdapter.getCount());
                                }
                                temp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                                        final int chosenTemp = Integer.parseInt(tempAdapter.getItem(position));
                                        if (chosenTemp != currTempContainer.get(0)) {
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        vehicle.vehicle.setTemp((double) chosenTemp);
                                                    } catch (final Exception e) {
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(VehicleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                        return;
                                                    }
                                                    currTempContainer.set(0, chosenTemp);
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(VehicleActivity.this, R.string.temp_set, Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }).start();
                                        }
                                    }
                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });

                                builder.setView(dialogView);
                                progressBar.cancel();
                                builder.show();
                            }
                        });
                    }
                }).start();

            }
        });

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
