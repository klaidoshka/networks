const endpointPrefix = 'http://127.0.0.1:23567/api/v1/graph';

const onGenerateNodesClick = () => {
  $('#generateNodesButton').click(() => {
    showFormModal(
        'Generate Nodes',
        [
          {
            id: 'amount',
            label: 'Enter the amount of nodes to generate:',
            type: 'number',
            required: true
          }
        ],
        (formData) => {
          const amount = formData.amount;

          if (amount > 0) {
            startLoad();

            $.post(
                `${endpointPrefix}/generate?amount=${amount}`,
                () => onSuccess('Nodes — generated')
            )
            .fail(onFail)
            .always(endLoad);
          } else {
            toastr.error('Invalid amount. Must be greater than 0');
          }
        });
  });
}

const onGenerateLeftSplitNodesClick = () => {
  $('#generateLeftSplitNodesButton').click(() => {
    showFormModal(
        'Generate Left Split Nodes',
        [
          {
            id: 'amount',
            label: 'Enter the amount of nodes to generate for the left split:',
            type: 'number',
            required: true
          }
        ],
        (formData) => {
          const amount = formData.amount;

          if (amount > 0) {
            startLoad();

            $.post(
                `${endpointPrefix}/generateLeftSplit?amount=${amount}`,
                () => onSuccess('Left split nodes — generated')
            )
            .fail(onFail)
            .always(endLoad);
          } else {
            toastr.error('Invalid amount. Must be greater than 0');
          }
        }
    );
  });
}

const onGenerateRightSplitNodesClick = () => {
  $('#generateRightSplitNodesButton').click(() => {
    showFormModal(
        'Generate Right Split Nodes',
        [
          {
            id: 'amount',
            label: 'Enter the amount of nodes to generate for the right split:',
            type: 'number',
            required: true
          }
        ],
        (formData) => {
          const amount = formData.amount;

          if (amount > 0) {
            startLoad();

            $.post(
                `${endpointPrefix}/generateRightSplit?amount=${amount}`,
                () => onSuccess('Right split nodes — generated')
            )
            .fail(onFail)
            .always(endLoad);
          } else {
            toastr.error('Invalid amount. Must be greater than 0');
          }
        }
    );
  });
}

const onDisplayGraphClick = () => {
  $('#displayGraphButton').click(() => {
    if (!$('#display').is(':empty')) {
      onSuccess('Graph — hidden');

      return;
    }

    startLoad();

    $
    .get(`${endpointPrefix}/display`, (data) => {
      toastr.success('Graph — displayed');

      console.log(data);

      const cy = cytoscape({
        container: document.getElementById('display'),
        elements: data.cytoscape,
        style: [
          {
            selector: 'node',
            style: {
              'background-color': '#b38f4f',
              'label': 'data(label)'
            }
          },
          {
            selector: 'edge',
            style: {
              'width': 3,
              'line-color': '#fff',
              'target-arrow-color': '#fff',
              'target-arrow-shape': 'triangle'
            }
          }
        ],
        layout: {
          name: 'cose',
          nodeRepulsion: 10000,
          idealEdgeLength: 100,
          edgeElasticity: 100,
          nestingFactor: 0.1,
        },
        maxZoom: 3,
        minZoom: 0.2,
        styleEnabled: true,
        wheelSensitive: 0.1
      });

      $('#display').css({
        'width': '100%',
        'height': '500px'
      });

      cy.resize();

      cy.fit();
    })
    .fail(onFail)
    .always(endLoad);
  });
}

const onDeleteGraphClick = () => {
  $('#deleteButton').click(() => {
    startLoad();

    $
    .post(
        `${endpointPrefix}/delete`,
        () => onSuccess('Graph — deleted')
    )
    .fail(onFail)
    .always(endLoad);
  });
}