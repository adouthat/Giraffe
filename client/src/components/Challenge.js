import {useState} from 'react';

export default function Challenge() {

const [cost, changeCost] = useState('0');
const [showTable, setTable] = useState(false);
const [data, setItem] = useState;

var restockItems = [];

const orderCost = () => {
  	console.log('Selected button restock cost')
  	fetch('http://localhost:4567/restock-cost', {
  	  method: 'POST',
  	  headers: {"Content-Type" : "application/json"},
  	  body: JSON.stringify(restockItems)
  	}
  	).then(response =>{

  	    return response.json();
  	}
  	).then(j =>{
  	    changeCost(j[0]["cost"])
  	})
  	restockItems = []
  	document.querySelectorAll('input').forEach(input => (input.value = ""));
  }
  return (
    <>
      <table>
        <thead>
          <tr>
            <td>SKU</td>
            <td>Item Name</td>
            <td>Amount in Stock</td>
            <td>Capacity</td>
            <td>Order Amount</td>
          </tr>
        </thead>
        <tbody>
          {/* 
          TODO: Create an <ItemRow /> component that's rendered for every inventory item. The component
          will need an input element in the Order Amount column that will take in the order amount and 
          update the application state appropriately.
          */}
        </tbody>
      </table>
      {/* TODO: Display total cost returned from the server */}
      <div>Total Cost:
        <p id="cost">{cost}</p>
      </div>

      {/* 
      TODO: Add event handlers to these buttons that use the Java API to perform their relative actions.
      */}
      <button>Get Low-Stock Items</button>
      <button>Determine Re-Order Cost</button>
    </>
  );
}
