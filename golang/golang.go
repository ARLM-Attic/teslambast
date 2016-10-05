package golang

import (
	"fmt"

	"github.com/zond/tesla"
)

type Connection struct {
	client *tesla.Client
}

func Connect(email, password string) (*Connection, error) {
	client, err := tesla.NewClient(
		&tesla.Auth{
			ClientID:     "e4a9949fcfa04068f59abb5a658f2bac0a3428e4652315490b659d5ab3f35a9e",
			ClientSecret: "c75f14bbadc8bee3a7594412c31416f8300256d7668ea7e6e7f06727bfb9d220",
			Email:        email,
			Password:     password,
		})
	if err != nil {
		return nil, err
	}
	return &Connection{
		client: client,
	}, nil
}

type Vehicle struct {
	ID   int64
	Name string

	vehicle    *tesla.Vehicle
	connection *Connection
	websocket  *tesla.WebSocket
}

func (v *Vehicle) MobileEnabled() (bool, error) {
	return v.vehicle.MobileEnabled()
}

type StateListener interface {
	tesla.WebSocketStateListener
}

func (v *Vehicle) Connect(listener StateListener) error {
	sock, err := v.vehicle.WebSocket(listener)
	if err != nil {
		return err
	}
	go func() {
		for _ = range sock.Output {
		}
	}()
	v.websocket = sock
	return nil
}

type ClimateState struct {
	On   bool
	Temp float64
}

func (v *Vehicle) ClimateState() (*ClimateState, error) {
	state, err := v.vehicle.ClimateState()
	if err != nil {
		return nil, err
	}
	return &ClimateState{
		On:   state.IsAutoConditioningOn,
		Temp: state.DriverTempSetting,
	}, nil
}

func (v *Vehicle) StartAirConditioning() error {
	return v.vehicle.StartAirConditioning()
}

func (v *Vehicle) StopAirConditioning() error {
	return v.vehicle.StopAirConditioning()
}

func (v *Vehicle) SetTemp(temp float64) error {
	return v.vehicle.SetTemperature(temp, temp)
}

func (v *Vehicle) AutoparkAbort() error {
	if v.websocket == nil {
		return fmt.Errorf("Not connected")
	}
	v.websocket.AutoparkAbort()
	return nil
}

func (v *Vehicle) AutoparkReverse() error {
	if v.websocket == nil {
		return fmt.Errorf("Not connected")
	}
	return v.websocket.AutoparkReverse()
}

func (v *Vehicle) AutoparkForward() error {
	if v.websocket == nil {
		return fmt.Errorf("Not connected")
	}
	return v.websocket.AutoparkForward()
}

func (v *Vehicle) ActivateHomelink() error {
	if v.websocket == nil {
		return fmt.Errorf("Not connected")
	}
	return v.websocket.ActivateHomelink()
}

func (v *Vehicle) Close() {
	v.websocket.Close()
}

func (v *Vehicle) UnlockCar() error {
	return v.vehicle.UnlockDoors()
}

func (v *Vehicle) UnlockCharger() error {
	return v.vehicle.OpenChargePort()
}

type Vehicles struct {
	Content *Vehicle
	Next    *Vehicles
}

func (c *Connection) Vehicles() (*Vehicles, error) {
	vehicles, err := c.client.Vehicles()
	if err != nil {
		return nil, err
	}
	result := &Vehicles{}
	next := result
	for index, vehicle := range vehicles {
		next.Content = &Vehicle{
			ID:         vehicle.Vehicle.ID,
			Name:       vehicle.Vehicle.DisplayName,
			vehicle:    vehicle.Vehicle,
			connection: c,
		}
		if index < len(vehicles)-1 {
			next.Next = &Vehicles{}
			next = next.Next
		}
	}
	return result, nil
}
